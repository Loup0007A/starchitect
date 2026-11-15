package com.starchitect.client;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.starchitect.Starchitect;
import com.starchitect.client.gui.GuideScreen;
import com.starchitect.client.gui.NewspaperScreen;
import com.starchitect.review.Critic;
import com.starchitect.review.Review;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Arrays;
import java.util.stream.Stream;

/** Commandes client : /starchitect review | guide | read | show | gauge | critic | rename | forget | reset */
@Mod.EventBusSubscriber(modid = Starchitect.MOD_ID, value = Dist.CLIENT)
public final class StarCommands {

    private StarCommands() {}

    private static void say(Component c) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) mc.player.displayClientMessage(c, false);
    }

    /** Ouvre un écran au tick suivant (le chat se ferme juste après la commande). */
    private static void later(Runnable r) {
        Minecraft mc = Minecraft.getInstance();
        mc.tell(() -> mc.tell(r));
    }

    private static Review nearest() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return null;
        BlockPos p = mc.player.blockPosition();
        return Guide.near(mc.level.dimension().location().toString(), p.getX(), p.getZ(), 32);
    }

    @SubscribeEvent
    public static void register(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("starchitect")
            .then(Commands.literal("review").executes(ctx -> {
                later(Visits::invite);
                return 1;
            }).then(Commands.argument("critic", StringArgumentType.word())
                .suggests((ctx, b) -> SharedSuggestionProvider.suggest(Arrays.stream(Critic.values()).map(Critic::id), b))
                .executes(ctx -> {
                    Critic c = Critic.byId(StringArgumentType.getString(ctx, "critic"));
                    if (c == null) {
                        say(Component.translatable("starchitect.cmd.unknown", StringArgumentType.getString(ctx, "critic")));
                        return 0;
                    }
                    later(() -> Visits.invite(c));
                    return 1;
                })))
            .then(Commands.literal("guide").executes(ctx -> {
                later(() -> Minecraft.getInstance().setScreen(new GuideScreen(null)));
                return 1;
            }))
            .then(Commands.literal("read").executes(ctx -> {
                Review r = nearest();
                if (r == null) {
                    say(Component.translatable("starchitect.cmd.none"));
                    return 0;
                }
                later(() -> Minecraft.getInstance().setScreen(new NewspaperScreen(r, null, false)));
                return 1;
            }))
            .then(Commands.literal("show").executes(ctx -> {
                Review r = nearest();
                if (r == null) {
                    say(Component.translatable("starchitect.cmd.none"));
                    return 0;
                }
                Highlights.show(r);
                return 1;
            }))
            .then(Commands.literal("gauge").executes(ctx -> {
                Gauge.toggle();
                return 1;
            }))
            .then(Commands.literal("critic")
                .then(Commands.argument("critic", StringArgumentType.word())
                    .suggests((ctx, b) -> SharedSuggestionProvider.suggest(
                        Stream.concat(Stream.of("random"), Arrays.stream(Critic.values()).map(Critic::id)), b))
                    .executes(ctx -> {
                        String id = StringArgumentType.getString(ctx, "critic");
                        if (id.equals("random")) {
                            Visits.forced = null;
                            say(Component.translatable("starchitect.cmd.critic", Component.translatable("starchitect.cmd.critic.random")));
                            return 1;
                        }
                        Critic c = Critic.byId(id);
                        if (c == null) {
                            say(Component.translatable("starchitect.cmd.unknown", id));
                            return 0;
                        }
                        Visits.forced = c;
                        say(Component.translatable("starchitect.cmd.critic", Texts.critic(c)));
                        return 1;
                    })))
            .then(Commands.literal("rename")
                .then(Commands.argument("name", StringArgumentType.greedyString()).executes(ctx -> {
                    Review r = nearest();
                    if (r == null) {
                        say(Component.translatable("starchitect.cmd.none"));
                        return 0;
                    }
                    String name = StringArgumentType.getString(ctx, "name").trim();
                    r.customName = name.length() > 48 ? name.substring(0, 48) : name;
                    Guide.save();
                    say(Component.translatable("starchitect.cmd.rename", Texts.name(r)));
                    return 1;
                })))
            .then(Commands.literal("forget").executes(ctx -> {
                Review r = nearest();
                if (r == null) {
                    say(Component.translatable("starchitect.cmd.none"));
                    return 0;
                }
                Guide.remove(r);
                say(Component.translatable("starchitect.cmd.forget", Texts.name(r)));
                return 1;
            }))
            .then(Commands.literal("reset").executes(ctx -> {
                Guide.reset();
                Awards.reset();
                say(Component.translatable("starchitect.cmd.reset"));
                return 1;
            })));
    }
}
