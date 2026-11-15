package com.starchitect.debug;

import java.util.ArrayList;
import java.util.List;

/**
 * Les constructions de test (CI et tools/OfflineCheck.java), sous forme de commandes /fill et /setblock.
 * Code pur : le serveur de test les exécute, la vérification hors ligne les interprète.
 */
public final class TestBuilds {

    private TestBuilds() {}

    /** Décalage de la cabane en terre par rapport à la maison (hors du rayon de la première critique). */
    public static final int HUT_OFFSET = 70;

    /** Une maison de chêne soignée, devant le joueur placé en (bx, by, bz) et regardant vers +X. */
    public static List<String> house(int bx, int by, int bz) {
        List<String> c = new ArrayList<>();
        int x1 = bx + 4, x2 = bx + 12, z1 = bz - 3, z2 = bz + 3;
        // terrain plat et dégagé
        c.add(fill(bx - 3, by, bz - 9, bx + 17, by + 14, bz + 9, "air"));
        c.add(fill(bx - 3, by - 3, bz - 9, bx + 17, by - 2, bz + 9, "dirt"));
        c.add(fill(bx - 3, by - 1, bz - 9, bx + 17, by - 1, bz + 9, "grass_block"));
        // soubassement, murs, piliers
        c.add(fill(x1, by - 1, z1, x2, by - 1, z2, "stone_bricks"));
        c.add(fill(x1, by, z1, x2, by + 3, z1, "oak_planks"));
        c.add(fill(x1, by, z2, x2, by + 3, z2, "oak_planks"));
        c.add(fill(x1, by, z1, x1, by + 3, z2, "oak_planks"));
        c.add(fill(x2, by, z1, x2, by + 3, z2, "oak_planks"));
        for (int x : new int[]{x1, x2}) {
            for (int z : new int[]{z1, z2}) c.add(fill(x, by, z, x, by + 3, z, "oak_log[axis=y]"));
        }
        // fenêtres (symétriques)
        for (int x : new int[]{x1 + 2, x1 + 3, x2 - 3, x2 - 2}) {
            c.add(fill(x, by + 1, z1, x, by + 2, z1, "glass_pane"));
            c.add(fill(x, by + 1, z2, x, by + 2, z2, "glass_pane"));
        }
        for (int z : new int[]{z1 + 1, z2 - 1}) {
            c.add(fill(x1, by + 1, z, x1, by + 2, z, "glass_pane"));
            c.add(fill(x2, by + 1, z, x2, by + 2, z, "glass_pane"));
        }
        // porte
        c.add(set(x1, by, bz, "oak_door[facing=west,half=lower]"));
        c.add(set(x1, by + 1, bz, "oak_door[facing=west,half=upper]"));
        c.add(set(x1 - 1, by - 1, bz, "stone_brick_stairs[facing=east]"));
        // pignons
        for (int k = 0; k < 4; k++) {
            int za = z1 + k, zb = z2 - k;
            if (za > zb) break;
            c.add(fill(x1, by + 4 + k, za, x1, by + 4 + k, zb, "oak_planks"));
            c.add(fill(x2, by + 4 + k, za, x2, by + 4 + k, zb, "oak_planks"));
        }
        // toit en escaliers de sapin, faîtage en dalles
        for (int k = 0; k < 4; k++) {
            int y = by + 4 + k;
            c.add(fill(x1 - 1, y, z1 - 1 + k, x2 + 1, y, z1 - 1 + k, "spruce_stairs[facing=south]"));
            c.add(fill(x1 - 1, y, z2 + 1 - k, x2 + 1, y, z2 + 1 - k, "spruce_stairs[facing=north]"));
        }
        c.add(fill(x1 - 1, by + 7, bz, x2 + 1, by + 7, bz, "spruce_planks"));
        c.add(fill(x1 - 1, by + 8, bz, x2 + 1, by + 8, bz, "spruce_slab[type=bottom]"));
        // intérieur
        c.add(set(x2 - 2, by, z1 + 1, "lantern"));
        c.add(set(x2 - 2, by, z2 - 1, "lantern"));
        c.add(set(x1 + 1, by, z2 - 1, "lantern"));
        c.add(set(x1 + 3, by + 1, z1 + 1, "lantern"));
        c.add(set(x2 - 1, by, bz - 1, "red_bed[facing=north,part=foot]"));
        c.add(set(x2 - 1, by, bz - 2, "red_bed[facing=north,part=head]"));
        c.add(set(x2 - 1, by, bz + 1, "crafting_table"));
        c.add(set(x2 - 1, by, bz + 2, "furnace[facing=west]"));
        c.add(set(x1 + 2, by, z1 + 1, "chest[facing=south]"));
        c.add(set(x1 + 3, by, z1 + 1, "bookshelf"));
        c.add(set(x1 + 4, by, z1 + 1, "bookshelf"));
        c.add(set(x1 + 2, by, z2 - 1, "barrel[facing=up]"));
        c.add(set(x1 + 3, by, z2 - 1, "potted_poppy"));
        c.add(fill(x1 + 3, by, bz - 1, x1 + 5, by, bz + 1, "red_carpet"));
        // jardin
        for (int z = z1 - 2; z <= z2 + 2; z += 2) {
            c.add(set(x1 - 3, by, z, z % 4 == 0 ? "poppy" : "dandelion"));
        }
        c.add(fill(x1 - 2, by, z1 - 3, x2, by, z1 - 3, "oak_leaves[persistent=true]"));
        c.add(fill(x1 - 2, by, z2 + 3, x2, by, z2 + 3, "oak_leaves[persistent=true]"));
        return c;
    }

    /** Une cabane en terre, sans fenêtre ni porte (une simple ouverture). */
    public static List<String> hut(int bx, int by, int bz) {
        List<String> c = new ArrayList<>();
        int x1 = bx + 3, x2 = bx + 7, z1 = bz - 2, z2 = bz + 2;
        c.add(fill(bx - 3, by, bz - 7, bx + 10, by + 10, bz + 7, "air"));
        c.add(fill(bx - 3, by - 3, bz - 7, bx + 10, by - 2, bz + 7, "dirt"));
        c.add(fill(bx - 3, by - 1, bz - 7, bx + 10, by - 1, bz + 7, "grass_block"));
        c.add(fill(x1, by, z1, x2, by + 3, z2, "dirt") + " hollow");
        c.add(fill(x1, by + 1, bz, x1, by + 2, bz, "air"));
        c.add(set(x2 - 1, by + 1, bz, "torch"));
        return c;
    }

    private static String fill(int x1, int y1, int z1, int x2, int y2, int z2, String block) {
        return "fill " + x1 + " " + y1 + " " + z1 + " " + x2 + " " + y2 + " " + z2 + " " + block;
    }

    private static String set(int x, int y, int z, String block) {
        return "setblock " + x + " " + y + " " + z + " " + block;
    }
}
