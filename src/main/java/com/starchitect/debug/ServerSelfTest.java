package com.starchitect.debug;

import com.starchitect.Starchitect;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Mise en scène pour l'intégration continue uniquement (-Dstarchitect.selftest=true) : une maison
 * soignée et une cabane en terre près du point d'apparition. Sans cette propriété, ne fait rien.
 */
@Mod.EventBusSubscriber(modid = Starchitect.MOD_ID)
public final class ServerSelfTest {

    private ServerSelfTest() {}

    private static final boolean ENABLED = Boolean.getBoolean("starchitect.selftest");

    @SubscribeEvent
    public static void onStarted(ServerStartedEvent event) {
        if (!ENABLED) return;
        MinecraftServer server = event.getServer();
        ServerLevel level = server.overworld();
        level.setDayTime(5000);
        level.setWeatherParameters(999999, 0, false, false);
        level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, server);
        level.getGameRules().getRule(GameRules.RULE_WEATHER_CYCLE).set(false, server);
        level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, server);
        Starchitect.LOGGER.info("[SELFTEST] READY");
    }

    private static void run(MinecraftServer server, String cmd) {
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(), cmd);
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!ENABLED || !(event.getEntity() instanceof ServerPlayer sp)) return;
        MinecraftServer server = sp.getServer();
        server.getPlayerList().op(sp.getGameProfile());
        sp.setGameMode(GameType.CREATIVE);
        ServerLevel level = sp.serverLevel();
        BlockPos spawn = level.getSharedSpawnPos();
        int by = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawn.getX(), spawn.getZ());
        int bx = spawn.getX(), bz = spawn.getZ();
        // 1.20.5+ : seuls 2 tronçons autour du point d'apparition restent chargés ; on génère ceux du décor
        for (int cx = (bx - 8) >> 4; cx <= (bx + TestBuilds.HUT_OFFSET + 16) >> 4; cx++) {
            for (int cz = (bz - 12) >> 4; cz <= (bz + 12) >> 4; cz++) level.getChunk(cx, cz);
        }
        for (String c : TestBuilds.house(bx, by, bz)) run(server, c);
        int hy = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, bx + TestBuilds.HUT_OFFSET, bz);
        for (String c : TestBuilds.hut(bx + TestBuilds.HUT_OFFSET, hy, bz)) run(server, c);
        // un chat, pour la remarque du critique
        run(server, "summon cat " + (bx + 7) + " " + by + " " + bz + " {NoAI:1b,PersistenceRequired:1b}");
        sp.teleportTo(level, bx + 0.5, by + 0.1, bz + 0.5, -90f, 15f);
        Starchitect.LOGGER.info("[SELFTEST] maison en {} {} {}, cabane à +{} (y {})", bx, by, bz, TestBuilds.HUT_OFFSET, hy);
    }
}
