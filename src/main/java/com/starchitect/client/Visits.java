package com.starchitect.client;

import com.starchitect.Starchitect;
import com.starchitect.analysis.Analyzer;
import com.starchitect.analysis.Survey;
import com.starchitect.client.gui.GuideScreen;
import com.starchitect.client.gui.NewspaperScreen;
import com.starchitect.client.gui.Portraits;
import com.starchitect.config.SConfig;
import com.starchitect.review.Critic;
import com.starchitect.review.Review;
import com.starchitect.review.Scoring;
import com.starchitect.review.Variants;
import com.starchitect.review.Writer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;
import java.util.UUID;

/**
 * La visite du critique : annonce (visite surprise), inspection (relevé des blocs, quelques secondes),
 * rédaction, puis publication dans le journal et dans le guide.
 */
@Mod.EventBusSubscriber(modid = Starchitect.MOD_ID, value = Dist.CLIENT)
public final class Visits {

    private Visits() {}

    public enum Phase { IDLE, ANNOUNCED, SCANNING, WRITING }

    private static Phase phase = Phase.IDLE;
    private static int timer;
    private static int phaseTicks;
    private static Critic critic = Critic.AUGUSTIN;
    private static boolean manual;
    private static Scanner scanner;
    private static Survey survey;
    private static BlockPos origin;
    private static String dim;
    private static String thought;
    private static int thoughtTicks;
    /** Ticks avant la prochaine visite surprise. */
    private static int autoTimer = -1;
    /** Critique imposé par la commande /starchitect critic (null = configuration). */
    public static Critic forced;
    private static final Random RND = new Random();

    public static Phase phase() {
        return phase;
    }

    public static Critic critic() {
        return critic;
    }

    private static Minecraft mc() {
        return Minecraft.getInstance();
    }

    private static void say(Component c) {
        if (mc().player != null) mc().player.displayClientMessage(c, false);
    }

    private static void sound(SoundEvent s, float pitch) {
        if (SConfig.SOUNDS.get()) mc().getSoundManager().play(SimpleSoundInstance.forUI(s, pitch));
    }

    private static String dimension(Level level) {
        return level.dimension().location().toString();
    }

    private static Critic chooseCritic(Review previous) {
        if (forced != null) return forced;
        Critic fixed = Critic.byId(SConfig.CRITIC.get().trim().toLowerCase(java.util.Locale.ROOT));
        if (fixed != null) return fixed;
        Critic[] all = Critic.values();
        Critic c = all[RND.nextInt(all.length)];
        if (previous != null && c.id().equals(previous.critic)) c = all[(c.ordinal() + 1 + RND.nextInt(all.length - 1)) % all.length];
        return c;
    }

    private static Scanner newScanner() {
        int r = SConfig.SCAN_RADIUS.get();
        return new Scanner(mc().level, mc().player.blockPosition(), r, 10, Math.max(16, r));
    }

    /** Touche J ou /starchitect review : le critique vient tout de suite. */
    public static void invite() {
        invite(null);
    }

    public static void invite(Critic who) {
        if (mc().player == null || mc().level == null) return;
        if (phase == Phase.SCANNING || phase == Phase.WRITING) {
            say(Component.translatable("starchitect.msg.busy"));
            return;
        }
        if (phase == Phase.ANNOUNCED) {
            // on accueille le critique annoncé sans attendre
            if (who != null) critic = who;
            startScan();
            return;
        }
        manual = true;
        BlockPos p = mc().player.blockPosition();
        critic = who != null ? who : chooseCritic(Guide.near(dimension(mc().level), p.getX(), p.getZ(), 24));
        startScan();
    }

    /** Pour le test automatique : une visite surprise annoncée tout de suite. */
    public static void debugAnnounce(Critic who) {
        critic = who;
        manual = false;
        phase = Phase.ANNOUNCED;
        timer = Math.max(1, SConfig.ANNOUNCE_SECONDS.get()) * 20;
        origin = mc().player.blockPosition();
        dim = dimension(mc().level);
    }

    public static void cancel() {
        phase = Phase.IDLE;
        scanner = null;
        survey = null;
    }

    private static void startScan() {
        phase = Phase.SCANNING;
        phaseTicks = 0;
        origin = mc().player.blockPosition();
        dim = dimension(mc().level);
        scanner = newScanner();
        thought = null;
        thoughtTicks = 30;
        sound(SoundEvents.BOOK_PAGE_TURN, 0.8f);
    }

    /** Visite surprise : on vérifie d'abord, en silence, qu'il y a du nouveau à critiquer. */
    private static void tryAutoVisit() {
        Scanner s = newScanner();
        s.step(Integer.MAX_VALUE, 0);
        Survey sv = Analyzer.analyze(s.grid, s.cx, s.cz, 12);
        if (sv.blocks < SConfig.MIN_BLOCKS.get()) {
            autoTimer = 20 * 120;
            return;
        }
        Review prev = Guide.near(dimension(mc().level), sv.centerX(), sv.centerZ(), 16);
        if (prev != null && prev.fingerprint == sv.fingerprint) {
            autoTimer = 20 * 120;
            return;
        }
        critic = chooseCritic(prev);
        manual = false;
        phase = Phase.ANNOUNCED;
        timer = SConfig.ANNOUNCE_SECONDS.get() * 20;
        origin = mc().player.blockPosition();
        dim = dimension(mc().level);
        say(Component.translatable("starchitect.msg.rumor").withStyle(ChatFormatting.ITALIC, ChatFormatting.GOLD));
        sound(SoundEvents.VILLAGER_AMBIENT, 1.4f);
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
        Minecraft mc = mc();
        if (mc.player == null || mc.level == null) {
            if (phase != Phase.IDLE) cancel();
            autoTimer = -1;
            return;
        }
        while (ClientBoot.KEY_INVITE.consumeClick()) invite();
        while (ClientBoot.KEY_GUIDE.consumeClick()) mc.setScreen(new GuideScreen(null));

        if (autoTimer < 0) autoTimer = SConfig.VISIT_INTERVAL.get() * 60 * 20;
        if (phase == Phase.IDLE) {
            if (SConfig.AUTO_VISITS.get() && mc.screen == null && mc.player.isAlive() && --autoTimer <= 0) {
                autoTimer = SConfig.VISIT_INTERVAL.get() * 60 * 20;
                try {
                    tryAutoVisit();
                } catch (Exception e) {
                    Starchitect.LOGGER.error("Starchitect : visite surprise impossible", e);
                }
            }
            return;
        }

        // Partir trop loin (ou changer de dimension) annule la visite
        int limit = SConfig.SCAN_RADIUS.get() + 12;
        if (!dimension(mc.level).equals(dim) || mc.player.blockPosition().distSqr(origin) > (double) limit * limit) {
            if (manual || phase != Phase.ANNOUNCED) say(Component.translatable("starchitect.msg.cancel"));
            cancel();
            return;
        }
        phaseTicks++;
        switch (phase) {
            case ANNOUNCED -> {
                if (--timer <= 0) startScan();
            }
            case SCANNING -> {
                // une inspection dure environ 5 secondes
                int cols = scanner.grid.sx * scanner.grid.sz;
                scanner.step(Math.max(1, cols / 100 + 1), SConfig.PARTICLES.get() ? 3 : 0);
                if (--thoughtTicks <= 0) {
                    thought = "starchitect.think." + RND.nextInt(Math.max(1, Variants.count("starchitect.think")));
                    thoughtTicks = 45 + RND.nextInt(25);
                    if (RND.nextInt(3) == 0) sound(SoundEvents.VILLAGER_AMBIENT, 0.9f + RND.nextFloat() * 0.3f);
                }
                if (scanner.done()) {
                    survey = Analyzer.analyze(scanner.grid, scanner.cx, scanner.cz, 12);
                    scanner = null;
                    if (survey.blocks < SConfig.MIN_BLOCKS.get()) {
                        if (manual) {
                            int n = Math.max(1, Variants.count("starchitect.msg.nothing"));
                            say(Component.translatable("starchitect.msg.nothing." + RND.nextInt(n), Texts.critic(critic)));
                            sound(SoundEvents.VILLAGER_NO, 1f);
                        }
                        cancel();
                        return;
                    }
                    phase = Phase.WRITING;
                    phaseTicks = 0;
                    timer = 40;
                    sound(SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 1f);
                }
            }
            case WRITING -> {
                if (--timer <= 0) publish();
            }
            default -> {}
        }
    }

    private static String timeKey(Level level) {
        long t = level.getDayTime() % 24000L;
        return t < 1000 || t >= 23000 ? "starchitect.time.morning" : t < 11500 ? "starchitect.time.day"
            : t < 13500 ? "starchitect.time.evening" : "starchitect.time.night";
    }

    private static void publish() {
        Minecraft mc = mc();
        Level level = mc.level;
        Survey s = survey;
        cancel();
        Review previous = Guide.near(dim(level), s.centerX(), s.centerZ(), 16);
        Scoring sc = Scoring.of(s, critic);
        String weather = level.isThundering() ? "starchitect.weather.thunder" : level.isRaining() ? "starchitect.weather.rain"
            : "starchitect.weather.clear";
        Review r = Writer.write(s, sc, critic,
            new Writer.Visit(timeKey(level), weather, level.getDayTime() / 24000L + 1, previous, level.getGameTime() ^ System.nanoTime()));
        r.uid = UUID.randomUUID().toString().substring(0, 8);
        r.dim = dim(level);
        r.x = s.centerX();
        r.y = s.centerY();
        r.z = s.centerZ();
        r.time = System.currentTimeMillis();
        Guide.publish(r, previous);
        Starchitect.LOGGER.info("Starchitect : {} → {} étoile(s) ({} / 100, {} blocs)", r.critic, r.stars, r.total, r.blocks);

        // Un petit feu d'artifice (ou un nuage de fumée) au-dessus de la construction
        ParticleOptions fx = r.dirtHut || r.stars == 0 ? ParticleTypes.LARGE_SMOKE : r.stars == 3 ? ParticleTypes.TOTEM_OF_UNDYING
            : ParticleTypes.HAPPY_VILLAGER;
        for (int i = 0; i < 40; i++) {
            double x = s.minX + RND.nextDouble() * (s.maxX - s.minX + 1);
            double z = s.minZ + RND.nextDouble() * (s.maxZ - s.minZ + 1);
            level.addParticle(fx, x, s.maxY + 1.2 + RND.nextDouble(), z, 0, 0.05, 0);
        }
        MutableComponent msg = Component.translatable("starchitect.msg.published", Texts.name(r).copy().withStyle(ChatFormatting.BOLD),
            Component.literal(Texts.stars(r.stars)).withStyle(ChatFormatting.GOLD));
        msg.withStyle(s2 -> s2.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/starchitect guide"))
            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("starchitect.guide.open"))));
        say(msg);
        mc.setScreen(new NewspaperScreen(r, null, true));
    }

    private static String dim(Level level) {
        return dimension(level);
    }

    // ───────────────────────── HUD ─────────────────────────

    @SubscribeEvent
    public static void onHud(RenderGuiEvent.Post event) {
        if (phase == Phase.IDLE || mc().options.hideGui || mc().screen != null) return;
        try {
            hud(event.getGuiGraphics());
        } catch (Exception e) {
            Starchitect.LOGGER.error("Starchitect : HUD", e);
            cancel();
        }
    }

    private static void hud(GuiGraphics g) {
        Minecraft mc = mc();
        Font font = mc.font;
        int sw = g.guiWidth();
        int w = 220, h = 46;
        int x = (sw - w) / 2, y = 6;
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xFF2B2520);
        g.fill(x, y, x + w, y + h, 0xF0F3EBD9);
        g.fill(x, y, x + 3, y + h, critic.color);
        Portraits.draw(g, critic, x + 8, y + 5, 3);
        int tx = x + 52;
        g.drawString(font, Texts.critic(critic).copy().withStyle(ChatFormatting.BOLD), tx, y + 5, 0xFF2B2520, false);
        Component status;
        float progress;
        switch (phase) {
            case ANNOUNCED -> {
                status = Component.translatable("starchitect.hud.arriving", Texts.critic(critic), (timer + 19) / 20);
                progress = 0;
                g.drawString(font, Component.translatable("starchitect.hud.arriving2", ClientBoot.KEY_INVITE.getTranslatedKeyMessage()),
                    tx, y + 34, 0xFF6E6252, false);
            }
            case SCANNING -> {
                status = Component.translatable("starchitect.hud.inspecting", Texts.critic(critic));
                progress = scanner == null ? 1 : scanner.progress();
            }
            default -> {
                status = Component.translatable("starchitect.hud.writing", Texts.critic(critic));
                progress = 1;
            }
        }
        g.drawString(font, font.split(status, w - 58).get(0), tx, y + 16, 0xFF4A4036, false);
        if (phase != Phase.ANNOUNCED) {
            int bx = tx, by = y + 29, bw = w - 60;
            g.fill(bx, by, bx + bw, by + 4, 0xFFD8CDB4);
            g.fill(bx, by, bx + (int) (bw * progress), by + 4, critic.color);
            if (phase == Phase.SCANNING && thought != null) {
                Component t = Component.translatable(thought).withStyle(ChatFormatting.ITALIC);
                g.drawString(font, font.split(t, w - 58).get(0), tx, y + 36, 0xFF6E6252, false);
            } else if (phase == Phase.WRITING) {
                String dots = ".".repeat(1 + (phaseTicks / 6) % 3);
                g.drawString(font, "✎ " + dots, tx, y + 36, 0xFF6E6252, false);
            }
        }
    }
}
