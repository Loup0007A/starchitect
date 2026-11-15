package com.starchitect.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** Notification « Distinction obtenue ! », sur un bandeau de papier avec un sceau. */
public class AwardToast implements Toast {

    private final String id;
    private boolean played;

    public AwardToast(String id) {
        this.id = id;
    }

    /** Le sceau d'une distinction. */
    public static ResourceLocation seal(String id) {
        return switch (id) {
            case "dirt_hut" -> Gfx.seal(0, true);
            case "three_stars", "perfect" -> Gfx.seal(3, false);
            case "first_star", "comeback" -> Gfx.seal(1, false);
            default -> Gfx.seal(2, false);
        };
    }

    @Override
    public Visibility render(GuiGraphics g, ToastComponent toasts, long time) {
        if (!played) {
            played = true;
            toasts.getMinecraft().getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                net.minecraft.sounds.SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.2f, 0.6f));
        }
        Font f = toasts.getMinecraft().font;
        int w = width(), h = height();
        Gfx.image(g, Gfx.PAPER_STRIP, 0, 0, w, h, 256, 64);
        g.renderOutline(0, 0, w, h, Gfx.INK);
        g.fill(0, 0, 3, h, 0xFFB07C14);
        // le sceau tombe sur le bandeau
        float k = Math.min(1f, time / 250f);
        float s = 24 * (1f + (1 - k) * (1 - k));
        Gfx.image(g, seal(id), 17 - s / 2, h / 2f - s / 2, s, s, 96, 96, Math.min(1f, k * 1.5f));
        g.drawString(f, Component.translatable("starchitect.award.toast").withStyle(ChatFormatting.BOLD), 34, 7, 0xFF8A5A10, false);
        g.drawString(f, Component.translatable("starchitect.award." + id), 34, 18, Gfx.INK, false);
        return time >= 5000L ? Visibility.HIDE : Visibility.SHOW;
    }
}
