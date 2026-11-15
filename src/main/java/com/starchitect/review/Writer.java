package com.starchitect.review;

import com.starchitect.analysis.Kind;
import com.starchitect.analysis.Survey;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Rédige la critique : un chapeau, les rubriques les plus marquantes, quelques remarques et le verdict.
 * Tout passe par des clés de traduction (voir tools/lang.py) ; le nombre de variantes de chaque phrase
 * vient de {@link Variants}, généré par le même script.
 */
public final class Writer {

    private Writer() {}

    /** Les circonstances de la visite. */
    public record Visit(String timeKey, String weatherKey, long day, Review previous, long seed) {}

    private static String pick(Random rnd, String prefix) {
        int n = Variants.count(prefix);
        return prefix + "." + (n <= 1 ? 0 : rnd.nextInt(n));
    }

    /** Type de construction, selon sa taille et sa forme. */
    public static String type(Survey s, Scoring sc) {
        if (sc.dirtHut) return "hut";
        if (Kind.family(s.dominant).equals("cobble") && s.glass == 0 && s.blocks >= 40) return "bunker";
        if (s.height >= 10 && s.height >= 2 * Math.max(s.width, s.depth)) return "tower";
        if (s.glassRatio() > 0.5) return "greenhouse";
        int b = s.blocks;
        return b < 80 ? "shack" : b < 250 ? "cottage" : b < 700 ? "house" : b < 1800 ? "villa" : b < 4000 ? "manor" : "palace";
    }

    public static Review write(Survey s, Scoring sc, Critic critic, Visit v) {
        Random rnd = new Random(v.seed() ^ s.fingerprint ^ critic.ordinal() * 0x51ED27L);
        Review r = new Review();
        r.critic = critic.id();
        r.type = type(s, sc);
        r.mat = sc.dirtHut ? "dirt" : Kind.family(s.dominant);
        r.stars = sc.stars;
        r.total = sc.total;
        r.scores = new int[Criterion.values().length];
        for (Criterion c : Criterion.values()) r.scores[c.ordinal()] = (int) Math.round(sc.score(c));
        r.day = v.day();
        r.fingerprint = s.fingerprint;
        r.blocks = s.blocks;
        r.dirtHut = sc.dirtHut;
        String name = "@name";
        if (v.previous() != null) {
            r.previousStars = v.previous().stars;
            r.visits = v.previous().visits + 1;
            r.customName = v.previous().customName;
        }

        r.headline = new Line(pick(rnd, "starchitect.headline." + (sc.dirtHut ? "dirt" : String.valueOf(sc.stars))), name);

        // Chapeau
        List<Line> body = r.body;
        body.add(new Line(pick(rnd, "starchitect.intro." + critic.id()), "#" + v.timeKey(), "#" + v.weatherKey(), name));
        if (v.previous() != null) {
            int d = Integer.compare(sc.stars, v.previous().stars);
            String trend = d > 0 ? "up" : d < 0 ? "down" : "same";
            body.add(new Line(pick(rnd, "starchitect.revisit." + trend), v.previous().stars, sc.stars, r.visits));
        }
        body.add(new Line(Line.BREAK));

        // Rubriques : les plus remarquables (très bonnes ou très mauvaises) d'abord
        List<Criterion> crits = new ArrayList<>(List.of(Criterion.values()));
        crits.sort(Comparator.comparingDouble((Criterion c) -> -Math.abs(sc.score(c) - 5.5) * critic.weight(c)));
        int quoteScore = -1;
        int written = 0;
        for (Criterion c : crits) {
            if (written >= 4) break;
            double score = sc.score(c);
            int tier = Scoring.tier(score);
            Line line = new Line(pick(rnd, "starchitect.c." + c.id() + "." + tier), args(s, c));
            body.add(line);
            written++;
            // Exergue : la pire phrase si elle est cinglante, sinon la meilleure
            int strength = tier == 0 ? 100 + (int) (10 - score) : tier == 3 ? 50 + (int) score : -1;
            if (strength > quoteScore) {
                quoteScore = strength;
                r.quote = body.size() - 1;
            }
            if (written == 2) body.add(new Line(Line.BREAK));
        }

        // Goûts du critique
        if (sc.dirtHut) {
            // la terre a sa propre remarque
        } else if (sc.hated >= 0.25 && sc.hatedFamily != null) {
            body.add(new Line(pick(rnd, "starchitect.hate." + critic.id()), "#starchitect.matn." + sc.hatedFamily));
        } else if (sc.loved >= 0.25 && sc.lovedFamily != null) {
            body.add(new Line(pick(rnd, "starchitect.love." + critic.id()), "#starchitect.matn." + sc.lovedFamily));
        }
        body.add(new Line(Line.BREAK));

        // Remarques
        int remarks = 0;
        for (Line l : remarks(s, sc, rnd)) {
            if (remarks++ >= 3) break;
            body.add(l);
        }
        if (remarks > 0) body.add(new Line(Line.BREAK));

        // Verdict
        body.add(new Line(pick(rnd, "starchitect.verdict." + (sc.dirtHut ? "dirt" : String.valueOf(sc.stars))), name, sc.stars));
        return r;
    }

    private static Object[] args(Survey s, Criterion c) {
        return switch (c) {
            case PALETTE -> new Object[]{s.materialCount, "#" + blockKey(s.dominantBlock), pct(s.dominantShare)};
            case DETAIL -> new Object[]{pct(s.detailRatio())};
            case LIGHT -> new Object[]{s.dark, s.lights};
            case WINDOWS -> new Object[]{pct(s.glassRatio()), s.glass};
            case ROOF -> new Object[]{pct(s.slopedRatio())};
            case SYMMETRY -> new Object[]{pct(s.symmetry)};
            case COMFORT -> new Object[]{s.furnitureKinds.size(),
                s.furnitureKinds.isEmpty() ? "#starchitect.nothing" : "#" + blockKey(s.furnitureKinds.get(0))};
            case GARDEN -> new Object[]{s.plants};
        };
    }

    /** Clé de traduction du nom d'un bloc ou d'un objet posé (« minecraft:painting » est une entité). */
    public static String blockKey(String id) {
        int c = id.indexOf(':');
        String ns = c < 0 ? "minecraft" : id.substring(0, c);
        String path = c < 0 ? id : id.substring(c + 1);
        boolean entity = path.equals("painting") || path.equals("item_frame") || path.equals("glow_item_frame")
            || path.equals("armor_stand");
        return (entity ? "entity." : "block.") + ns + "." + path;
    }

    private static int pct(double v) {
        return (int) Math.round(v * 100);
    }

    private static List<Line> remarks(Survey s, Scoring sc, Random rnd) {
        List<Line> out = new ArrayList<>();
        if (sc.dirtHut) out.add(new Line(pick(rnd, "starchitect.remark.dirt")));
        if (s.doors == 0 && s.interior > 0) out.add(new Line(pick(rnd, "starchitect.remark.nodoor")));
        if (s.standable > 0 && s.darkRatio() > 0.3) out.add(new Line(pick(rnd, "starchitect.remark.mobs"), s.dark));
        if (s.interior == 0 && s.blocks > 40) out.add(new Line(pick(rnd, "starchitect.remark.noroof")));
        if (s.cat) out.add(new Line(pick(rnd, "starchitect.remark.cat")));
        if (s.dog) out.add(new Line(pick(rnd, "starchitect.remark.dog")));
        if (s.parrot) out.add(new Line(pick(rnd, "starchitect.remark.parrot")));
        if (s.villager) out.add(new Line(pick(rnd, "starchitect.remark.villager")));
        if (s.golem) out.add(new Line(pick(rnd, "starchitect.remark.golem")));
        if (s.horse) out.add(new Line(pick(rnd, "starchitect.remark.horse")));
        if (s.dyeColors >= 6) out.add(new Line(pick(rnd, "starchitect.remark.rainbow"), s.dyeColors));
        if (!sc.dirtHut && s.share("cobble") > 0.6) out.add(new Line(pick(rnd, "starchitect.remark.cobble")));
        if (s.glassRatio() > 0.5) out.add(new Line(pick(rnd, "starchitect.remark.greenhouse")));
        if (s.height >= 10 && s.height >= 2 * Math.max(s.width, s.depth)) out.add(new Line(pick(rnd, "starchitect.remark.tower"), s.height));
        if (s.blocks < 60) out.add(new Line(pick(rnd, "starchitect.remark.tiny"), s.blocks));
        if (s.blocks > 2500) out.add(new Line(pick(rnd, "starchitect.remark.giant"), s.blocks));
        if (s.share("gold") + s.share("diamond") + s.share("emerald") + s.share("netherite") > 0.1) {
            out.add(new Line(pick(rnd, "starchitect.remark.bling")));
        }
        return out;
    }
}
