package com.starchitect.review;

import com.starchitect.analysis.Kind;
import com.starchitect.analysis.Survey;

import java.util.Map;

/** Transforme un relevé en notes (0 à 10 par rubrique), en note globale (0 à 100) et en étoiles. */
public final class Scoring {

    public final double[] scores = new double[Criterion.values().length];
    public int total;
    public int stars;
    /** Part des matériaux adorés / détestés par le critique (0 à 1). */
    public double loved, hated;
    public String lovedFamily, hatedFamily;
    public boolean dirtHut;
    public com.starchitect.analysis.Style style;
    /** Le critique adore ce style. */
    public boolean styleLoved;

    public double score(Criterion c) {
        return scores[c.ordinal()];
    }

    public static Scoring of(Survey s, Critic critic) {
        Scoring r = new Scoring();
        r.scores[Criterion.PALETTE.ordinal()] = palette(s);
        r.scores[Criterion.DETAIL.ordinal()] = detail(s);
        r.scores[Criterion.LIGHT.ordinal()] = light(s);
        r.scores[Criterion.WINDOWS.ordinal()] = windows(s);
        r.scores[Criterion.ROOF.ordinal()] = roof(s);
        r.scores[Criterion.SYMMETRY.ordinal()] = symmetry(s);
        r.scores[Criterion.COMFORT.ordinal()] = comfort(s);
        r.scores[Criterion.GARDEN.ordinal()] = garden(s);

        double sum = 0, weights = 0;
        for (Criterion c : Criterion.values()) {
            sum += critic.weight(c) * r.score(c);
            weights += critic.weight(c);
        }
        double total = sum / weights * 10.0;

        // Goûts du critique
        double bestLove = 0, bestHate = 0;
        for (Map.Entry<String, Integer> e : s.materials.entrySet()) {
            String fam = Kind.family(e.getKey());
            double share = (double) e.getValue() / Math.max(1, s.structural);
            if (critic.loves.contains(fam)) {
                r.loved += share;
                if (share > bestLove) {
                    bestLove = share;
                    r.lovedFamily = fam;
                }
            }
            if (critic.hates.contains(fam)) {
                r.hated += share;
                if (share > bestHate) {
                    bestHate = share;
                    r.hatedFamily = fam;
                }
            }
        }
        total += 12 * Math.min(1, r.loved * 2) - 12 * Math.min(1, r.hated * 2);

        // Style
        r.style = com.starchitect.analysis.Style.detect(s);
        r.styleLoved = critic.styles.contains(r.style.id());
        if (r.styleLoved) total += 4;

        // Petits plus, gros moins
        if (s.cat || s.dog || s.parrot) total += 3;
        if (s.doors == 0 && s.interior > 0) total -= 5;
        if (s.blocks < 60) total *= 0.9;
        if (s.dirtRatio() > 0.3) {
            r.dirtHut = true;
            total = Math.min(total * 0.4, 25);
        }
        r.total = (int) Math.round(Math.max(0, Math.min(100, total)));
        r.stars = r.total >= 82 ? 3 : r.total >= 64 ? 2 : r.total >= 42 ? 1 : 0;
        return r;
    }

    /** Palier d'une note : 0 = raté, 1 = moyen, 2 = bien, 3 = remarquable. */
    public static int tier(double score) {
        return score < 4 ? 0 : score < 7 ? 1 : score < 9 ? 2 : 3;
    }

    static double palette(Survey s) {
        if (s.dirtRatio() > 0.3) return 0;
        int m = s.materialCount;
        double v = m <= 1 ? 2 : m == 2 ? 4.5 : m == 3 ? 7 : m <= 9 ? 10 : m <= 14 ? 8 : m <= 20 ? 6 : 4;
        if (s.dominantShare > 0.85) v = Math.min(v, 3);
        else if (s.dominantShare > 0.7) v -= 2;
        if (s.dyeColors >= 7) v -= 2;
        return clamp(v);
    }

    static double detail(Survey s) {
        double r = s.detailRatio();
        return r < 0.03 ? 1 : r < 0.08 ? 4 : r < 0.15 ? 7 : r <= 0.4 ? 10 : r <= 0.6 ? 7 : 4;
    }

    static double light(Survey s) {
        if (s.standable == 0) return s.lights == 0 ? 3 : 7;
        double d = s.darkRatio();
        double v = d == 0 ? 10 : d < 0.1 ? 8.5 : d < 0.3 ? 5.5 : d < 0.6 ? 3 : 1;
        if (s.lights == 0) v = Math.min(v, 4);
        return v;
    }

    static double windows(Survey s) {
        double g = s.glassRatio();
        if (s.glass == 0) return s.blocks < 60 ? 3 : 0.5;
        return g < 0.03 ? 3 : g < 0.06 ? 6 : g <= 0.25 ? 10 : g <= 0.4 ? 7 : 4;
    }

    static double roof(Survey s) {
        if (s.roofColumns == 0) return 1.5;
        double r = s.slopedRatio();
        return r >= 0.6 ? 10 : r >= 0.3 ? 7.5 : r >= 0.1 ? 5 : 3;
    }

    static double symmetry(Survey s) {
        double v = s.symmetry;
        return v >= 0.9 ? 10 : v >= 0.75 ? 8 : v >= 0.6 ? 6 : v >= 0.4 ? 4 : 2;
    }

    static double comfort(Survey s) {
        int f = s.furnitureKinds.size();
        double v = f >= 8 ? 10 : f >= 6 ? 8.5 : f >= 4 ? 6.5 : f >= 2 ? 4.5 : f == 1 ? 2.5 : 0.5;
        return s.interior == 0 ? v * 0.7 : v;
    }

    static double garden(Survey s) {
        int p = s.plants;
        return p == 0 ? 1 : p < 4 ? 4 : p < 10 ? 7 : p < 25 ? 9 : 10;
    }

    private static double clamp(double v) {
        return Math.max(0, Math.min(10, v));
    }
}
