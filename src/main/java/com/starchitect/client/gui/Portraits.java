package com.starchitect.client.gui;

import com.starchitect.review.Critic;
import net.minecraft.client.gui.GuiGraphics;

/** Portraits des critiques, en pixels (aucune texture). */
public final class Portraits {

    private Portraits() {}

    private static final String[] AUGUSTIN = {
        "...KKKKKK...",
        "...KKKKKK...",
        "...PPPPPP...",
        ".KKKKKKKKKK.",
        "..SSSSSSSS..",
        "..SSSSSGGG..",
        "..SEESSGEG..",
        "..SSSsSGGG..",
        "..SBBBBBBS..",
        "..SSSRRSSS..",
        "...SSSSSS...",
        "..WWWKKWWW..",
    };
    private static final String[] BRIGITTE = {
        "...gggggg...",
        "..gggggggg..",
        ".gggggggggg.",
        ".ggSSSSSSgg.",
        ".gSSSSSSSSg.",
        ".gKKKSSKKKg.",
        ".gKEKKKKEKg.",
        ".gKKKSsKKKg.",
        ".gSSSSSSSSg.",
        ".ggSSRRSSgg.",
        "..gSSSSSSg..",
        "..KKKKKKKK..",
    };
    private static final String[] GASTON = {
        "....YYYY....",
        "...YYYYYY...",
        "...BBBBBB...",
        "YYYYYYYYYYYY",
        "..SSSSSSSS..",
        "..SEESSEES..",
        "..RSSssSSR..",
        "..WSSSSSSW..",
        "..WWWWWWWW..",
        "..WWWRRWWW..",
        "...WWWWWW...",
        "..BBYBBYBB..",
    };
    private static final String[] ZOE = {
        "...TTTTTT...",
        "..TTTTTTTT..",
        ".TTTTTTTTTT.",
        ".TTTTTTSSST.",
        ".TSSSSSSSST.",
        "KTSWWSSWWSTK",
        "KTSWESSWESTK",
        ".TSSSSSSSST.",
        ".TSSSsSSSST.",
        ".TSSSRRSSST.",
        "..TSSSSSST..",
        "..TTMMMMTT..",
    };

    private static int color(char c) {
        return switch (c) {
            case 'S' -> 0xFFE3B990;
            case 's' -> 0xFFC9976C;
            case 'K' -> 0xFF1C1A1A;
            case 'W' -> 0xFFF2F0EA;
            case 'B' -> 0xFF5C3B1E;
            case 'G' -> 0xFFE2B33A;
            case 'R' -> 0xFFB23A3A;
            case 'g' -> 0xFF8B9097;
            case 'Y' -> 0xFFE6C66A;
            case 'T' -> 0xFF2CA8A2;
            case 'E' -> 0xFF26263A;
            case 'P' -> 0xFF6B2D5C;
            case 'M' -> 0xFFD0458E;
            default -> 0;
        };
    }

    private static String[] of(Critic c) {
        return switch (c) {
            case AUGUSTIN -> AUGUSTIN;
            case BRIGITTE -> BRIGITTE;
            case GASTON -> GASTON;
            case ZOE -> ZOE;
        };
    }

    /** Dessine le portrait (12×12 pixels) dans un médaillon, à l'échelle {@code px}. */
    public static void draw(GuiGraphics g, Critic critic, int x, int y, int px) {
        int size = 12 * px;
        g.fill(x - 2, y - 2, x + size + 2, y + size + 2, 0xFF2B2520);
        g.fill(x - 1, y - 1, x + size + 1, y + size + 1, critic.color);
        g.fill(x, y, x + size, y + size, 0xFFE9DFC8);
        String[] rows = of(critic);
        for (int r = 0; r < rows.length; r++) {
            String row = rows[r];
            for (int col = 0; col < row.length(); col++) {
                int c = color(row.charAt(col));
                if (c != 0) g.fill(x + col * px, y + r * px, x + (col + 1) * px, y + (r + 1) * px, c);
            }
        }
    }
}
