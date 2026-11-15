package com.starchitect.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.starchitect.Starchitect;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/** Petits outils de dessin partagés par les écrans : textures, papier, étoiles, sceaux, mise en page du texte. */
public final class Gfx {

    private Gfx() {}

    public static final ResourceLocation PAPER = tex("paper");
    public static final ResourceLocation PAPER_STRIP = tex("paper_strip");
    public static final ResourceLocation STAR = tex("star");
    public static final ResourceLocation STAR_EMPTY = tex("star_empty");
    public static final ResourceLocation DIVIDER = tex("divider");
    public static final ResourceLocation MAGNIFIER = tex("magnifier");
    public static final ResourceLocation NO_PHOTO = tex("no_photo");

    public static final int INK = 0xFF2B2520;
    public static final int GREY = 0xFF6E6252;
    public static final int RULE = 0xFF8C7F6A;
    public static final int GOLD = 0xFFB07C14;

    private static ResourceLocation tex(String name) {
        return Starchitect.id("textures/gui/" + name + ".png");
    }

    public static ResourceLocation seal(int stars, boolean dirt) {
        return tex(dirt ? "seal_dirt" : "seal_" + Math.max(0, Math.min(3, stars)));
    }

    /** Une texture entière étirée sur (x, y, w, h), avec transparence et teinte/opacité. */
    public static void image(GuiGraphics g, ResourceLocation t, float x, float y, float w, float h, int texW, int texH, float alpha) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale(w / texW, h / texH, 1f);
        g.blit(t, 0, 0, 0f, 0f, texW, texH, texW, texH);
        g.pose().popPose();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    public static void image(GuiGraphics g, ResourceLocation t, float x, float y, float w, float h, int texW, int texH) {
        image(g, t, x, y, w, h, texW, texH, 1f);
    }

    /** Feuille de papier vieilli, avec ombre douce et double filet. */
    public static void paper(GuiGraphics g, int x, int y, int w, int h, boolean strip) {
        for (int i = 4; i >= 1; i--) g.fill(x + i + 1, y + i + 2, x + w + i + 1, y + h + i + 2, 0x18000000);
        if (strip) image(g, PAPER_STRIP, x, y, w, h, 256, 64);
        else image(g, PAPER, x, y, w, h, 512, 320);
        g.renderOutline(x, y, w, h, INK);
    }

    public static void frame(GuiGraphics g, int x, int y, int w, int h) {
        g.renderOutline(x + 3, y + 3, w - 6, h - 6, RULE);
    }

    /** Ornement horizontal (filet avec losange) centré sur cx. */
    public static void divider(GuiGraphics g, int cx, int y, int w) {
        image(g, DIVIDER, cx - w / 2f, y - 4, w, 8, 96, 8);
    }

    /** Trois étoiles (pleines ou vides) ; {@code pop} = étoile en train d'apparaître (0..1, -1 = aucune). */
    public static void stars(GuiGraphics g, int x, int y, int size, int filled, int popIndex, float pop) {
        for (int i = 0; i < 3; i++) {
            float s = size;
            if (i == popIndex && pop >= 0) s = size * (1f + 0.8f * (float) Math.sin(Math.min(1f, pop) * Math.PI));
            float cx = x + i * (size + 2) + size / 2f, cy = y + size / 2f;
            image(g, i < filled ? STAR : STAR_EMPTY, cx - s / 2f, cy - s / 2f, s, s, 16, 16);
        }
    }

    /** Texte agrandi. */
    public static void big(GuiGraphics g, Font f, Component c, float x, float y, float scale, int color) {
        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale(scale, scale, 1f);
        g.drawString(f, c, 0, 0, color, false);
        g.pose().popPose();
    }

    public interface Widths {
        int at(int line);
    }

    /** Coupe un texte en lignes (largeur variable d'une ligne à l'autre, pour la lettrine). */
    public static List<String> wrap(Font f, String text, Widths widths) {
        List<String> out = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            if (word.isEmpty()) continue;
            String candidate = line.isEmpty() ? word : line + " " + word;
            if (f.width(candidate) <= widths.at(out.size()) || line.isEmpty()) {
                line.setLength(0);
                line.append(candidate);
                // mot plus long que la ligne : coupé net
                while (f.width(line.toString()) > widths.at(out.size()) && line.length() > 1) {
                    String head = f.plainSubstrByWidth(line.toString(), widths.at(out.size()));
                    if (head.isEmpty()) break;
                    out.add(head);
                    line.delete(0, head.length());
                }
            } else {
                out.add(line.toString());
                line.setLength(0);
                line.append(word);
            }
        }
        if (!line.isEmpty()) out.add(line.toString());
        return out;
    }
}
