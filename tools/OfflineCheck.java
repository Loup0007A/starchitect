import com.starchitect.analysis.Analyzer;
import com.starchitect.analysis.Grid;
import com.starchitect.analysis.Kind;
import com.starchitect.analysis.Survey;
import com.starchitect.debug.TestBuilds;
import com.starchitect.review.Critic;
import com.starchitect.review.Criterion;
import com.starchitect.review.Line;
import com.starchitect.review.Review;
import com.starchitect.review.Scoring;
import com.starchitect.review.Writer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Vérification hors du jeu : construit les maisons de test dans une grille, les fait critiquer et
 * affiche la critique. Usage : bash tools/offline.sh [fr|en]
 */
public class OfflineCheck {

    static Map<String, String> lang;

    public static void main(String[] args) throws Exception {
        String l = args.length > 0 ? args[0] : "en";
        lang = parse(Files.readString(Path.of("src/main/resources/assets/starchitect/lang/" + (l.equals("fr") ? "fr_fr" : "en_us") + ".json")));
        int fails = 0;
        for (Critic c : Critic.values()) {
            Review house = run("house", TestBuilds.house(0, 64, 0), c, c == Critic.AUGUSTIN);
            Review hut = run("hut", TestBuilds.hut(0, 64, 0), c, c == Critic.AUGUSTIN);
            System.out.println(c + " : maison " + house.stars + "★ (" + house.total + ")  cabane " + hut.stars + "★ (" + hut.total + ")");
            if (house.stars < 2) fails++;
            if (hut.stars != 0 || !hut.dirtHut) fails++;
        }
        System.out.println(fails == 0 ? "OFFLINE OK" : "OFFLINE ÉCHEC (" + fails + ")");
        System.exit(fails == 0 ? 0 : 1);
    }

    static Review run(String name, List<String> cmds, Critic critic, boolean print) {
        Grid g = new Grid(-20, 56, -20, 41, 33, 41);
        // terrain naturel : pierre et terre
        for (int y = 0; y < 8; y++) for (int z = 0; z < g.sz; z++) for (int x = 0; x < g.sx; x++) {
            String b = y < 4 ? "stone" : y < 7 ? "dirt" : "grass_block";
            g.set(x, y, z, "minecraft:" + b, Kind.classify(b, false, (char) 0, 0), 0);
        }
        Pattern blockRe = Pattern.compile("([a-z_]+)(\\[(.*)\\])?");
        for (String cmd : cmds) {
            String[] p = cmd.split(" ");
            boolean fill = p[0].equals("fill");
            int x1 = Integer.parseInt(p[1]), y1 = Integer.parseInt(p[2]), z1 = Integer.parseInt(p[3]);
            int x2 = fill ? Integer.parseInt(p[4]) : x1, y2 = fill ? Integer.parseInt(p[5]) : y1, z2 = fill ? Integer.parseInt(p[6]) : z1;
            String block = p[fill ? 7 : 4];
            boolean hollow = fill && p.length > 8 && p[8].equals("hollow");
            Matcher m = blockRe.matcher(block);
            m.matches();
            String path = m.group(1);
            String props = m.group(3) == null ? "" : m.group(3);
            boolean persistent = props.contains("persistent=true");
            char axis = props.contains("axis=x") ? 'x' : props.contains("axis=z") ? 'z' : props.contains("axis=y") ? 'y'
                : path.endsWith("_log") ? 'y' : 0;
            int emission = path.contains("lantern") ? 15 : path.contains("torch") ? 14 : 0;
            byte k = Kind.classify(path, persistent, axis, emission);
            for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++)
                for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z++)
                    for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
                        boolean edge = x == Math.min(x1, x2) || x == Math.max(x1, x2) || y == Math.min(y1, y2)
                            || y == Math.max(y1, y2) || z == Math.min(z1, z2) || z == Math.max(z1, z2);
                        String put = hollow && !edge ? "air" : path;
                        byte kk = hollow && !edge ? Kind.AIR : k;
                        int lx = x - g.ox, ly = y - g.oy, lz = z - g.oz;
                        if (g.inside(lx, ly, lz)) g.set(lx, ly, lz, "minecraft:" + put, kk, 0);
                    }
        }
        light(g);
        Survey s = Analyzer.analyze(g, 20, 20, 12);
        Scoring sc = Scoring.of(s, critic);
        Review r = Writer.write(s, sc, critic, new Writer.Visit("starchitect.time.day", "starchitect.weather.clear", 3, null, 42));
        if (print) {
            System.out.printf("== %s : %d blocs, %d×%d×%d, matériaux %s, dominant %s (%.0f%%)%n", name, s.blocks, s.width,
                s.height, s.depth, s.materials, s.dominant, s.dominantShare * 100);
            System.out.printf("   relief %.2f vitrage %.2f portes %d lumières %d meubles %s plantes %d%n", s.detailRatio(),
                s.glassRatio(), s.doors, s.lights, s.furnitureKinds, s.plants);
            System.out.printf("   intérieur %d debout %d noir %d toit %d/%d symétrie %.2f terre %.2f%n", s.interior, s.standable,
                s.dark, s.slopedColumns, s.roofColumns, s.symmetry, s.dirtRatio());
            StringBuilder sb = new StringBuilder("   notes :");
            for (Criterion c : Criterion.values()) sb.append(' ').append(c.id()).append('=').append(String.format("%.1f", sc.score(c)));
            System.out.println(sb + "  total " + sc.total + " → " + sc.stars + "★");
            String nm = render(new Line("starchitect.name." + r.type, "#starchitect.mat." + r.mat), null);
            System.out.println("   # " + render(r.headline, nm));
            StringBuilder para = new StringBuilder("   ");
            for (Line line : r.body) {
                if (line.isBreak()) {
                    System.out.println(para);
                    para = new StringBuilder("   ");
                } else para.append(render(line, nm)).append(' ');
            }
            if (para.length() > 3) System.out.println(para);
            System.out.println("   > " + (r.quote >= 0 ? render(r.body.get(r.quote), nm) : "-"));
        }
        return r;
    }

    /** Lumière des blocs, propagée comme dans le jeu (−1 par case, arrêtée par les murs). */
    static void light(Grid g) {
        ArrayDeque<int[]> q = new ArrayDeque<>();
        for (int i = 0; i < g.kind.length; i++) {
            String p = g.path(i);
            int e = p.contains("lantern") ? 15 : p.contains("torch") ? 14 : 0;
            if (e > 0) {
                g.light[i] = (byte) e;
                q.add(new int[]{i, e});
            }
        }
        while (!q.isEmpty()) {
            int[] c = q.poll();
            int i = c[0], x = i % g.sx, z = (i / g.sx) % g.sz, y = i / (g.sx * g.sz);
            int[][] d = {{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};
            for (int[] o : d) {
                int X = x + o[0], Y = y + o[1], Z = z + o[2];
                if (!g.inside(X, Y, Z)) continue;
                int j = g.at(X, Y, Z);
                byte k = g.kind[j];
                boolean opaque = k == Kind.SOLID || k == Kind.NATURAL || k == Kind.DIRT || k == Kind.LOG || k == Kind.PLACED_DIRT;
                if (opaque) continue;
                int v = c[1] - 1;
                if (v > g.light[j]) {
                    g.light[j] = (byte) v;
                    q.add(new int[]{j, v});
                }
            }
        }
    }

    static String render(Line line, String name) {
        String fmt = lang.getOrDefault(line.key, line.key);
        Object[] a = new Object[line.args.length];
        for (int i = 0; i < a.length; i++) {
            String s = line.args[i];
            a[i] = s.equals("@name") ? name : s.startsWith("#") ? lang.getOrDefault(s.substring(1), s.substring(1)) : s;
        }
        try {
            return String.format(fmt, a);
        } catch (Exception e) {
            return "!!" + fmt;
        }
    }

    static Map<String, String> parse(String json) {
        Map<String, String> m = new java.util.HashMap<>();
        Matcher mm = Pattern.compile("\"((?:[^\"\\\\]|\\\\.)*)\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"").matcher(json);
        while (mm.find()) m.put(mm.group(1), mm.group(2).replace("\\\"", "\""));
        return m;
    }
}
