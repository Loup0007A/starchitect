package com.starchitect.client;

import com.starchitect.Starchitect;
import com.starchitect.review.Review;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

/** « Montrer » : les coins dans le noir d'une critique, signalés par des particules rouges pendant 30 secondes. */
@Mod.EventBusSubscriber(modid = Starchitect.MOD_ID, value = Dist.CLIENT)
public final class Highlights {

    private Highlights() {}

    private static final DustParticleOptions RED = new DustParticleOptions(new Vector3f(0.95f, 0.12f, 0.1f), 1.6f);
    private static int[] spots = new int[0];
    private static String dim;
    private static int ticks;

    public static int remaining() {
        return ticks;
    }

    public static int count() {
        return spots.length / 3;
    }

    public static void show(Review r) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (r.darkSpots == null || r.darkSpots.length < 3) {
            mc.player.displayClientMessage(Component.translatable("starchitect.show.none"), false);
            return;
        }
        spots = r.darkSpots;
        dim = r.dim;
        ticks = 30 * 20;
        mc.player.displayClientMessage(Component.translatable("starchitect.show.started", spots.length / 3), true);
    }

    public static void stop() {
        ticks = 0;
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
        if (ticks <= 0) return;
        ticks--;
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null || mc.isPaused() || !level.dimension().location().toString().equals(dim)) return;
        if (ticks % 4 != 0) return;
        for (int i = 0; i + 2 < spots.length; i += 3) {
            double x = spots[i] + 0.5, y = spots[i + 1] + 0.3, z = spots[i + 2] + 0.5;
            level.addParticle(RED, x, y, z, 0, 0.02, 0);
            if (ticks % 20 == 0) level.addParticle(ParticleTypes.SMOKE, x, y + 0.2, z, 0, 0.01, 0);
        }
    }
}
