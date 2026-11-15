package com.starchitect.debug;

import com.starchitect.Starchitect;
import com.starchitect.client.Guide;
import com.starchitect.client.Visits;
import com.starchitect.client.gui.GuideScreen;
import com.starchitect.client.gui.NewspaperScreen;
import com.starchitect.review.Critic;
import com.starchitect.review.Review;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.BooleanSupplier;

/**
 * Test client (CI uniquement, -Dstarchitect.clienttest=true) : fait critiquer la maison de test puis la
 * cabane en terre, vérifie les étoiles et photographie chaque écran.
 */
@Mod.EventBusSubscriber(modid = Starchitect.MOD_ID, value = Dist.CLIENT)
public final class ClientSelfTest {

    private ClientSelfTest() {}

    private static final boolean ENABLED = Boolean.getBoolean("starchitect.clienttest");

    private record Step(String label, BooleanSupplier ready, Runnable action, int minWait) {}

    private static final Deque<Step> STEPS = new ArrayDeque<>();
    private static boolean built;
    private static int inGame, waited, failures;
    private static BlockPos base;
    private static int hutY;

    private static Minecraft mc() {
        return Minecraft.getInstance();
    }

    private static void step(String label, int wait, Runnable action) {
        STEPS.add(new Step(label, () -> true, action, wait));
    }

    private static void stepWhen(String label, BooleanSupplier ready, int wait, Runnable action) {
        STEPS.add(new Step(label, ready, action, wait));
    }

    private static void shot(String name) {
        Minecraft mc = mc();
        Screenshot.grab(mc.gameDirectory, name + ".png", mc.getMainRenderTarget(),
            c -> Starchitect.LOGGER.info("[CLIENTTEST] capture {}", name));
    }

    private static void check(boolean ok, String what) {
        if (!ok) {
            failures++;
            Starchitect.LOGGER.error("[CLIENTTEST] ÉCHEC : {}", what);
        } else {
            Starchitect.LOGGER.info("[CLIENTTEST] ok : {}", what);
        }
    }

    private static NewspaperScreen paper() {
        return mc().screen instanceof NewspaperScreen n ? n : null;
    }

    private static void build() {
        built = true;
        base = mc().player.blockPosition();
        step("monde", 1, () -> {
            Guide.reset();
            com.starchitect.client.Awards.reset();
            shot("00_world");
        });
        step("visite maison", 1, () -> Visits.invite(Critic.AUGUSTIN));
        step("inspection", 40, () -> {
            check(Visits.phase() == Visits.Phase.SCANNING, "inspection en cours");
            shot("01_inspecting");
        });
        stepWhen("journal maison", () -> paper() != null && paper().revealed(), 20, () -> {
            Review r = paper().review();
            Starchitect.LOGGER.info("[CLIENTTEST] maison : {} étoile(s), {}/100, {} blocs", r.stars, r.total, r.blocks);
            check(r.stars >= 2, "la maison obtient au moins 2 étoiles (" + r.stars + ")");
            check(!r.dirtHut, "la maison n'est pas une cabane en terre");
            check(r.photo != null && com.starchitect.client.Photos.texture(r.photo) != null, "photo de presse prise");
            shot("02_review_house");
        });
        step("défilement", 1, () -> paper().scrollBy(-20));
        step("défilement-capture", 4, () -> shot("02b_review_house_scrolled"));
        step("mètre", 1, () -> {
            mc().setScreen(null);
            com.starchitect.client.Gauge.toggle();
        });
        step("mètre-capture", 100, () -> {
            check(com.starchitect.client.Gauge.active(), "mètre de chantier actif");
            shot("02c_gauge");
            com.starchitect.client.Gauge.toggle();
        });
        step("guide", 1, () -> mc().setScreen(new GuideScreen(null)));
        step("guide-capture", 10, () -> shot("03_guide"));
        step("vers la cabane", 1, () -> {
            mc().setScreen(null);
            mc().player.connection.sendCommand("spreadplayers " + (base.getX() + TestBuilds.HUT_OFFSET) + " " + base.getZ()
                + " 0 1 false @s");
        });
        stepWhen("arrivée", () -> Math.abs(mc().player.getX() - (base.getX() + TestBuilds.HUT_OFFSET)) < 4, 40, () -> {
            hutY = mc().player.blockPosition().getY();
            mc().player.connection.sendCommand("tp @s ~ ~ ~ -90 15");
        });
        step("visite cabane", 40, () -> Visits.invite(Critic.BRIGITTE));
        stepWhen("journal cabane", () -> paper() != null && paper().revealed(), 20, () -> {
            Review r = paper().review();
            Starchitect.LOGGER.info("[CLIENTTEST] cabane : {} étoile(s), {}/100, {} blocs", r.stars, r.total, r.blocks);
            check(r.dirtHut, "la cabane en terre est reconnue");
            check(r.stars == 0, "la cabane n'a aucune étoile");
            check(r.darkSpots.length >= 3, "coins sombres relevés (" + r.darkSpots.length / 3 + ")");
            check(r.tips.size() >= 2, "conseils du critique (" + r.tips.size() + ")");
            shot("04_review_hut");
        });
        step("défilement-cabane", 1, () -> paper().scrollBy(-40));
        step("défilement-cabane-capture", 4, () -> shot("04b_review_hut_tips"));
        step("montrer", 1, () -> {
            Review r = paper().review();
            mc().setScreen(null);
            com.starchitect.client.Highlights.show(r);
            mc().player.connection.sendCommand("tp @s ~-2 ~ ~ -90 30");
        });
        step("montrer-capture", 40, () -> {
            check(com.starchitect.client.Highlights.remaining() > 0, "coins sombres montrés");
            shot("04c_show_dark_spots");
            com.starchitect.client.Highlights.stop();
        });
        // La cabane est rénovée (TestBuilds.renovation) puis critiquée à nouveau : avant / après
        step("rénovation", 1, () -> {
            for (String c : TestBuilds.renovation(base.getX() + TestBuilds.HUT_OFFSET, hutY, base.getZ())) {
                mc().player.connection.sendCommand(c);
            }
        });
        step("revisite", 30, () -> Visits.invite(Critic.GASTON));
        stepWhen("journal revisite", () -> paper() != null && paper().revealed(), 20, () -> {
            Review r = paper().review();
            Starchitect.LOGGER.info("[CLIENTTEST] cabane rénovée : {} étoile(s), {}/100, style {}", r.stars, r.total, r.style);
            check(!r.dirtHut, "la cabane rénovée n'est plus en terre");
            check(r.previousStars == 0 && r.visits == 2, "deuxième visite reconnue");
            check(r.previousPhoto != null && com.starchitect.client.Photos.texture(r.previousPhoto) != null, "photo d'avant conservée");
            check(r.style != null, "style reconnu");
            shot("07_before_after");
        });
        step("impression", 1, () -> paper().printForTest());
        step("impression-vérif", 40, () -> {
            java.io.File[] f = new java.io.File(mc().gameDirectory, "screenshots").listFiles((d, n) -> n.startsWith("starchitect_"));
            check(f != null && f.length > 0, "une imprimée");
            mc().setScreen(null);
        });
        step("distinctions", 60, () -> {
            for (String id : new String[]{"first_review", "first_star", "three_stars", "dirt_hut", "comeback"}) {
                check(com.starchitect.client.Awards.has(id), "distinction " + id);
            }
            shot("08_award_toasts");
        });
        step("guide-2", 1, () -> mc().setScreen(new GuideScreen(null)));
        step("guide-2-capture", 10, () -> {
            check(Guide.all().size() == 2, "deux critiques dans le guide (" + Guide.all().size() + ")");
            shot("05_guide_two");
            mc().setScreen(null);
        });
        step("annonce", 2, () -> Visits.debugAnnounce(Critic.ZOE));
        step("annonce-capture", 30, () -> {
            check(Visits.phase() == Visits.Phase.ANNOUNCED, "visite surprise annoncée");
            shot("06_announce");
            Visits.cancel();
        });
        step("fin", 10, () -> {
            Guide.save();
            Starchitect.LOGGER.info("[CLIENTTEST] RESULT: {}", failures == 0 ? "OK" : "FAIL " + failures);
            mc().stop();
        });
    }

    //#if mc1201
    //$$ @SubscribeEvent
    //$$ public static void onTick(TickEvent.ClientTickEvent event) {
    //$$     if (event.phase == TickEvent.Phase.END) tick();
    //$$ }
    //#else
    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent.Post event) {
        tick();
    }
    //#endif

    private static void tick() {
        if (!ENABLED) return;
        Minecraft mc = mc();
        if (mc.player == null || mc.level == null) return;
        inGame++;
        if (inGame < 160) return;
        if (!built) {
            build();
            Starchitect.LOGGER.info("[CLIENTTEST] début ({} étapes, cible {})", STEPS.size(), com.starchitect.platform.Platform.TARGET);
        }
        Step s = STEPS.peek();
        if (s == null) return;
        waited++;
        if (waited < s.minWait || !s.ready.getAsBoolean()) {
            if (waited > 1200) {
                check(false, "délai dépassé à l'étape « " + s.label + " »");
                STEPS.poll();
                waited = 0;
            }
            return;
        }
        STEPS.poll();
        waited = 0;
        try {
            s.action.run();
        } catch (Throwable t) {
            check(false, "exception à l'étape « " + s.label + " » : " + t);
            Starchitect.LOGGER.error("[CLIENTTEST]", t);
        }
    }
}
