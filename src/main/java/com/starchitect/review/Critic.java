package com.starchitect.review;

import java.util.Locale;
import java.util.Set;

/**
 * Les critiques du guide. Chacun a ses priorités (poids des rubriques) et ses goûts (familles de
 * matériaux qu'il adore ou déteste).
 */
public enum Critic {
    //                   palette detail light windows roof  sym  comfort garden
    AUGUSTIN(0xFF6B2D5C, new double[]{1.2, 1.3, 0.8, 1.0, 1.2, 1.6, 0.8, 0.8},
        Set.of("stone", "quartz", "brick", "deepslate", "sandstone"), Set.of("cobble", "wool", "dirt", "mystery")),
    BRIGITTE(0xFF5A6470, new double[]{0.8, 0.7, 1.0, 0.9, 0.6, 1.5, 0.9, 0.5},
        Set.of("concrete", "stone", "deepslate", "blackstone", "iron", "tuff"),
        Set.of("oak", "spruce", "birch", "jungle", "acacia", "cherry", "wool", "dirt")),
    GASTON(0xFF7A4E22, new double[]{1.0, 1.0, 1.0, 0.8, 1.3, 0.6, 1.4, 1.6},
        Set.of("oak", "spruce", "dark_oak", "birch", "cobble", "mossy", "mud"), Set.of("concrete", "quartz", "iron", "dirt")),
    ZOE(0xFF1F8A8A, new double[]{1.1, 0.9, 1.5, 1.6, 0.6, 1.0, 1.1, 0.9},
        Set.of("glass", "quartz", "concrete", "copper", "cherry", "birch"), Set.of("cobble", "mossy", "dirt", "mud"));

    /** Couleur du critique (bandeau, portrait). */
    public final int color;
    private final double[] weights;
    public final Set<String> loves, hates;

    Critic(int color, double[] weights, Set<String> loves, Set<String> hates) {
        this.color = color;
        this.weights = weights;
        this.loves = loves;
        this.hates = hates;
    }

    public double weight(Criterion c) {
        return weights[c.ordinal()];
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String nameKey() {
        return "starchitect.critic." + id();
    }

    public static Critic byId(String id) {
        for (Critic c : values()) if (c.id().equals(id)) return c;
        return null;
    }
}
