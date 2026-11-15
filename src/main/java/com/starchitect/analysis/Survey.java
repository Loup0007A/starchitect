package com.starchitect.analysis;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Le relevé du critique : tout ce qu'il a mesuré sur une construction. */
public final class Survey {

    /** Nombre de blocs de la construction (0 = rien trouvé). */
    public int blocks;
    /** Boîte englobante, coordonnées du monde. */
    public int minX, minY, minZ, maxX, maxY, maxZ;
    public int width, height, depth;

    /** Blocs de structure (hors lumières et mobilier). */
    public int structural;
    /** Matériau -> nombre de blocs, du plus présent au moins présent. */
    public final Map<String, Integer> materials = new LinkedHashMap<>();
    /** Matériaux qui comptent (au moins 2 % de la structure ou 3 blocs). */
    public int materialCount;
    public String dominant = "mystery";
    /** Un bloc représentatif du matériau dominant (identifiant complet, pour son nom traduit). */
    public String dominantBlock = "minecraft:stone";
    public double dominantShare;
    public int dyeColors;

    public int shapes, stairs, glass, doors, lights, furniture, placedDirt;
    public final List<String> furnitureKinds = new ArrayList<>();
    public int plants;

    /** Cases d'air intérieures, dont celles où l'on tient debout, dont celles dans le noir complet. */
    public int interior, standable, dark;
    /** Coordonnées (x, y, z à la suite) de quelques cases intérieures dans le noir complet. */
    public final List<Integer> darkSpots = new ArrayList<>();
    /** Colonnes couvertes d'un toit, dont celles dont le haut est un escalier ou une dalle. */
    public int roofColumns, slopedColumns;
    public double symmetry;

    public boolean cat, dog, parrot, villager, golem, horse;

    /** Empreinte de la construction : change dès qu'un bloc change. */
    public long fingerprint;

    public boolean empty() {
        return blocks == 0;
    }

    public double detailRatio() {
        return structural == 0 ? 0 : (double) (shapes + stairs) / structural;
    }

    public double glassRatio() {
        int walls = structural - stairs;
        return walls <= 0 ? 0 : (double) glass / walls;
    }

    public double slopedRatio() {
        return roofColumns == 0 ? 0 : (double) slopedColumns / roofColumns;
    }

    public double darkRatio() {
        return standable == 0 ? 0 : (double) dark / standable;
    }

    public double dirtRatio() {
        return structural == 0 ? 0 : (double) placedDirt / structural;
    }

    public double share(String family) {
        if (structural == 0) return 0;
        int n = 0;
        for (Map.Entry<String, Integer> e : materials.entrySet()) {
            if (Kind.family(e.getKey()).equals(family)) n += e.getValue();
        }
        return (double) n / structural;
    }

    public int centerX() {
        return (minX + maxX) / 2;
    }

    public int centerY() {
        return minY;
    }

    public int centerZ() {
        return (minZ + maxZ) / 2;
    }
}
