package com.starchitect.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Instantané d'une boîte de blocs autour du joueur : nature, identifiant et lumière de chaque case. */
public final class Grid {

    public final int ox, oy, oz;
    public final int sx, sy, sz;
    public final byte[] kind;
    /** Indice dans {@link #ids} du bloc de chaque case. */
    public final short[] id;
    /** Lumière des blocs (torches…) de chaque case, 0-15. */
    public final byte[] light;
    public final List<String> ids = new ArrayList<>();
    private final Map<String, Short> index = new HashMap<>();
    /** Créatures et objets posés (tableaux, cadres…) repérés dans la boîte. */
    public final List<Thing> things = new ArrayList<>();

    public record Thing(String type, int x, int y, int z, boolean tame) {}

    public Grid(int ox, int oy, int oz, int sx, int sy, int sz) {
        this.ox = ox;
        this.oy = oy;
        this.oz = oz;
        this.sx = sx;
        this.sy = sy;
        this.sz = sz;
        int n = sx * sy * sz;
        kind = new byte[n];
        id = new short[n];
        light = new byte[n];
        idOf("air");
    }

    public short idOf(String path) {
        Short s = index.get(path);
        if (s != null) return s;
        short v = (short) ids.size();
        ids.add(path);
        index.put(path, v);
        return v;
    }

    public boolean inside(int x, int y, int z) {
        return x >= 0 && y >= 0 && z >= 0 && x < sx && y < sy && z < sz;
    }

    public int at(int x, int y, int z) {
        return (y * sz + z) * sx + x;
    }

    /** Coordonnées locales ; hors de la boîte = de l'air. */
    public byte kind(int x, int y, int z) {
        return inside(x, y, z) ? kind[at(x, y, z)] : Kind.AIR;
    }

    public String path(int i) {
        return ids.get(id[i]);
    }

    public void set(int x, int y, int z, String path, byte k, int blockLight) {
        int i = at(x, y, z);
        kind[i] = k;
        id[i] = idOf(path);
        light[i] = (byte) blockLight;
    }
}
