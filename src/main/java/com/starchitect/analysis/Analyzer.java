package com.starchitect.analysis;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Trouve la construction la plus proche dans un instantané et la mesure. Code pur : testable hors du jeu.
 */
public final class Analyzer {

    private Analyzer() {}

    private static String path(String id) {
        int c = id.indexOf(':');
        return c < 0 ? id : id.substring(c + 1);
    }

    private static boolean airish(byte k) {
        return k == Kind.AIR || k == Kind.SOFT || k == Kind.LIGHT || k == Kind.FURNITURE || k == Kind.PLANT;
    }

    /**
     * @param g      l'instantané
     * @param cx     position du joueur dans la boîte (coordonnées locales)
     * @param cz     idem
     * @param near   distance horizontale maximale entre le joueur et la construction choisie
     */
    public static Survey analyze(Grid g, int cx, int cz, int near) {
        resolve(g);
        propagateLight(g);
        int n = g.kind.length;
        int[] comp = new int[n];
        List<int[]> comps = new ArrayList<>(); // {taille, distance min²}
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        int label = 0;
        for (int i = 0; i < n; i++) {
            if (comp[i] != 0 || !Kind.built(g.kind[i])) continue;
            label++;
            int size = 0;
            int best = Integer.MAX_VALUE;
            comp[i] = label;
            queue.add(i);
            while (!queue.isEmpty()) {
                int c = queue.poll();
                size++;
                int x = c % g.sx, z = (c / g.sx) % g.sz, y = c / (g.sx * g.sz);
                int d = (x - cx) * (x - cx) + (z - cz) * (z - cz);
                if (d < best) best = d;
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        for (int dx = -1; dx <= 1; dx++) {
                            if (dx == 0 && dy == 0 && dz == 0) continue;
                            int X = x + dx, Y = y + dy, Z = z + dz;
                            if (!g.inside(X, Y, Z)) continue;
                            int j = g.at(X, Y, Z);
                            if (comp[j] == 0 && Kind.built(g.kind[j])) {
                                comp[j] = label;
                                queue.add(j);
                            }
                        }
                    }
                }
            }
            comps.add(new int[]{size, best});
        }
        int chosen = 0;
        int chosenSize = 0;
        for (int pass = 0; pass < 2 && chosen == 0; pass++) {
            for (int c = 0; c < comps.size(); c++) {
                int[] info = comps.get(c);
                if (pass == 0 && info[1] > near * near) continue;
                if (info[0] > chosenSize) {
                    chosenSize = info[0];
                    chosen = c + 1;
                }
            }
        }
        Survey s = new Survey();
        if (chosen == 0) return s;
        measure(g, comp, chosen, s);
        return s;
    }

    /** Décide quels troncs sont des piliers et quelle terre a été posée. */
    private static void resolve(Grid g) {
        for (int pass = 0; pass < 2; pass++) {
            for (int y = 0; y < g.sy; y++) {
                for (int z = 0; z < g.sz; z++) {
                    for (int x = 0; x < g.sx; x++) {
                        int i = g.at(x, y, z);
                        if (g.kind[i] != Kind.LOG) continue;
                        if (builtNeighbour(g, x, y, z)) g.kind[i] = Kind.SOLID;
                    }
                }
            }
        }
        List<Integer> placed = new ArrayList<>();
        for (int y = 0; y < g.sy; y++) {
            for (int z = 0; z < g.sz; z++) {
                for (int x = 0; x < g.sx; x++) {
                    int i = g.at(x, y, z);
                    if (g.kind[i] != Kind.DIRT) continue;
                    byte below = g.kind(x, y - 1, z);
                    boolean floating = y > 0 && (below == Kind.AIR || below == Kind.SOFT || below == Kind.LIGHT);
                    boolean xa = airish(g.kind(x - 1, y, z)), xb = airish(g.kind(x + 1, y, z));
                    boolean za = airish(g.kind(x, y, z - 1)), zb = airish(g.kind(x, y, z + 1));
                    int open = (xa ? 1 : 0) + (xb ? 1 : 0) + (za ? 1 : 0) + (zb ? 1 : 0);
                    if (floating || (xa && xb) || (za && zb) || open >= 3) placed.add(i);
                }
            }
        }
        for (int i : placed) g.kind[i] = Kind.PLACED_DIRT;
    }

    /** Bloque la lumière (approximation : blocs pleins, escaliers et dalles). */
    private static boolean opaque(byte k) {
        return k == Kind.SOLID || k == Kind.NATURAL || k == Kind.DIRT || k == Kind.LOG || k == Kind.PLACED_DIRT
            || k == Kind.STAIR;
    }

    /**
     * Lumière des blocs, propagée comme dans le jeu (−1 par case). En entrée, {@link Grid#light} contient
     * la lumière émise par chaque bloc ; en sortie, la lumière reçue. Calculée ici plutôt que lue dans le
     * jeu : le client n'a pas toujours fini de l'éclairer, et le résultat ne dépend ainsi que des blocs.
     */
    public static void propagateLight(Grid g) {
        java.util.ArrayDeque<Integer> q = new java.util.ArrayDeque<>();
        for (int i = 0; i < g.light.length; i++) if (g.light[i] > 1) q.add(i);
        int[][] d = {{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};
        while (!q.isEmpty()) {
            int i = q.poll();
            int x = i % g.sx, z = (i / g.sx) % g.sz, y = i / (g.sx * g.sz);
            int v = g.light[i] - 1;
            if (v <= 0) continue;
            for (int[] o : d) {
                int X = x + o[0], Y = y + o[1], Z = z + o[2];
                if (!g.inside(X, Y, Z)) continue;
                int j = g.at(X, Y, Z);
                if (opaque(g.kind[j]) || g.light[j] >= v) continue;
                g.light[j] = (byte) v;
                q.add(j);
            }
        }
    }

    private static boolean builtNeighbour(Grid g, int x, int y, int z) {
        byte[] ks = {g.kind(x - 1, y, z), g.kind(x + 1, y, z), g.kind(x, y - 1, z), g.kind(x, y + 1, z),
            g.kind(x, y, z - 1), g.kind(x, y, z + 1)};
        for (byte k : ks) if (Kind.built(k)) return true;
        return false;
    }

    private static void measure(Grid g, int[] comp, int c, Survey s) {
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        Map<String, Integer> mats = new HashMap<>();
        Map<String, Integer> blockCount = new HashMap<>();
        Set<String> dyes = new LinkedHashSet<>();
        Set<String> furn = new LinkedHashSet<>();
        long fp = 0;
        int[] matIndex = new int[g.ids.size()];
        Map<String, Integer> matIds = new HashMap<>();
        for (int k = 0; k < g.ids.size(); k++) {
            matIndex[k] = matIds.computeIfAbsent(Kind.material(path(g.ids.get(k))), m -> matIds.size());
        }
        for (int i = 0; i < comp.length; i++) {
            if (comp[i] != c) continue;
            int x = i % g.sx, z = (i / g.sx) % g.sz, y = i / (g.sx * g.sz);
            minX = Math.min(minX, x); maxX = Math.max(maxX, x);
            minY = Math.min(minY, y); maxY = Math.max(maxY, y);
            minZ = Math.min(minZ, z); maxZ = Math.max(maxZ, z);
            s.blocks++;
            byte k = g.kind[i];
            String id = g.ids.get(g.id[i]);
            String p = path(id);
            long h = (g.ox + x) * 73856093L ^ (g.oy + y) * 19349663L ^ (g.oz + z) * 83492791L ^ id.hashCode() * 2654435761L;
            fp += h * 0x9E3779B97F4A7C15L + (h >>> 17);
            if (Kind.structural(k)) {
                s.structural++;
                String m = k == Kind.PLACED_DIRT ? "dirt" : Kind.material(p);
                mats.merge(m, 1, Integer::sum);
                blockCount.merge(id, 1, Integer::sum);
                String dye = Kind.dye(p);
                if (dye != null) dyes.add(dye);
            }
            switch (k) {
                case Kind.SHAPE -> s.shapes++;
                case Kind.STAIR -> s.stairs++;
                case Kind.GLASS -> s.glass++;
                case Kind.DOOR -> s.doors++;
                case Kind.LIGHT -> s.lights++;
                case Kind.FURNITURE -> {
                    s.furniture++;
                    furn.add(id);
                }
                case Kind.PLACED_DIRT -> s.placedDirt++;
                default -> {}
            }
        }
        s.fingerprint = fp;
        s.doors = (s.doors + 1) / 2;
        s.minX = g.ox + minX; s.maxX = g.ox + maxX;
        s.minY = g.oy + minY; s.maxY = g.oy + maxY;
        s.minZ = g.oz + minZ; s.maxZ = g.oz + maxZ;
        s.width = maxX - minX + 1;
        s.height = maxY - minY + 1;
        s.depth = maxZ - minZ + 1;
        s.dyeColors = dyes.size();

        // Palette
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(mats.entrySet());
        sorted.sort((a, b) -> b.getValue() - a.getValue());
        for (Map.Entry<String, Integer> e : sorted) {
            s.materials.put(e.getKey(), e.getValue());
            if (e.getValue() >= 3 && e.getValue() >= s.structural * 0.02) s.materialCount++;
        }
        if (!sorted.isEmpty()) {
            s.dominant = sorted.get(0).getKey();
            int best = -1;
            for (Map.Entry<String, Integer> e : blockCount.entrySet()) {
                String m = e.getKey().endsWith("dirt") || e.getKey().endsWith("grass_block") ? "dirt" : Kind.material(path(e.getKey()));
                if (m.equals(s.dominant) && e.getValue() > best) {
                    best = e.getValue();
                    s.dominantBlock = e.getKey();
                }
            }
            s.dominantShare = (double) sorted.get(0).getValue() / Math.max(1, s.structural);
        }

        // Créatures et objets posés
        for (Grid.Thing t : g.things) {
            int x = t.x() - g.ox, y = t.y() - g.oy, z = t.z() - g.oz;
            boolean in = x >= minX - 1 && x <= maxX + 1 && z >= minZ - 1 && z <= maxZ + 1 && y >= minY - 1 && y <= maxY + 1;
            boolean around = x >= minX - 6 && x <= maxX + 6 && z >= minZ - 6 && z <= maxZ + 6 && y >= minY - 3 && y <= maxY + 3;
            switch (t.type()) {
                case "painting", "item_frame", "glow_item_frame", "armor_stand" -> {
                    if (in) furn.add("minecraft:" + t.type());
                }
                case "cat" -> s.cat |= around;
                case "wolf" -> s.dog |= around && t.tame();
                case "parrot" -> s.parrot |= around && t.tame();
                case "villager" -> s.villager |= around;
                case "iron_golem" -> s.golem |= around;
                case "horse", "donkey", "mule", "camel" -> s.horse |= around && t.tame();
                default -> {}
            }
        }
        s.furnitureKinds.addAll(furn);

        // Jardin : autour de la construction
        for (int y = Math.max(0, minY - 2); y <= Math.min(g.sy - 1, maxY + 1); y++) {
            for (int z = Math.max(0, minZ - 4); z <= Math.min(g.sz - 1, maxZ + 4); z++) {
                for (int x = Math.max(0, minX - 4); x <= Math.min(g.sx - 1, maxX + 4); x++) {
                    if (g.kind[g.at(x, y, z)] == Kind.PLANT) s.plants++;
                }
            }
        }

        // Intérieur, éclairage, toiture
        int w = maxX - minX + 1, d = maxZ - minZ + 1;
        int[] top = new int[w * d];
        java.util.Arrays.fill(top, -1);
        for (int y = minY; y <= maxY; y++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int x = minX; x <= maxX; x++) {
                    if (comp[g.at(x, y, z)] == c) top[(z - minZ) * w + (x - minX)] = y;
                }
            }
        }
        boolean[] roofed = new boolean[w * d];
        for (int y = minY; y <= maxY; y++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int x = minX; x <= maxX; x++) {
                    int i = g.at(x, y, z);
                    if (g.kind[i] != Kind.AIR) continue;
                    int col = (z - minZ) * w + (x - minX);
                    if (top[col] <= y) continue;
                    int walls = 0;
                    int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
                    for (int[] dir : dirs) {
                        for (int step = 1; step <= 12; step++) {
                            int X = x + dir[0] * step, Z = z + dir[1] * step;
                            if (X < minX || X > maxX || Z < minZ || Z > maxZ) break;
                            if (Kind.wall(g.kind[g.at(X, y, Z)])) {
                                walls++;
                                break;
                            }
                        }
                    }
                    if (walls < 3) continue;
                    s.interior++;
                    roofed[col] = true;
                    byte below = g.kind(x, y - 1, z);
                    if (below != Kind.AIR && below != Kind.SOFT && below != Kind.LIGHT) {
                        s.standable++;
                        if (g.light[i] == 0) {
                            s.dark++;
                            if (s.darkSpots.size() < 48 * 3) {
                                s.darkSpots.add(g.ox + x);
                                s.darkSpots.add(g.oy + y);
                                s.darkSpots.add(g.oz + z);
                            }
                        }
                    }
                }
            }
        }
        for (int col = 0; col < w * d; col++) {
            if (!roofed[col]) continue;
            s.roofColumns++;
            int x = minX + col % w, z = minZ + col / w;
            byte k = g.kind[g.at(x, top[col], z)];
            if (k == Kind.STAIR) s.slopedColumns++;
        }

        // Symétrie (miroir selon l'axe X ou l'axe Z de la boîte)
        int mx = 0, mz = 0, total = 0;
        for (int y = minY; y <= maxY; y++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int x = minX; x <= maxX; x++) {
                    int i = g.at(x, y, z);
                    if (comp[i] != c) continue;
                    total++;
                    int a = g.at(minX + maxX - x, y, z);
                    int b = g.at(x, y, minZ + maxZ - z);
                    if (comp[a] == c && same(g, matIndex, i, a)) mx++;
                    if (comp[b] == c && same(g, matIndex, i, b)) mz++;
                }
            }
        }
        s.symmetry = total == 0 ? 0 : (double) Math.max(mx, mz) / total;
    }

    private static boolean same(Grid g, int[] matIndex, int i, int j) {
        byte a = g.kind[i], b = g.kind[j];
        if (a == Kind.LIGHT || a == Kind.FURNITURE || b == Kind.LIGHT || b == Kind.FURNITURE) return a == b;
        return matIndex[g.id[i]] == matIndex[g.id[j]];
    }
}
