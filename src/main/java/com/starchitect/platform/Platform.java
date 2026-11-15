package com.starchitect.platform;

/**
 * Ce qui diffère d'un loader / d'une version de Minecraft à l'autre. Cette version-ci est celle de
 * Forge 1.21–1.21.1 ; chaque autre cible a la sienne dans targets/&lt;cible&gt;/overrides (voir tools/port.py).
 */
public final class Platform {

    private Platform() {}

    /** Nom de la cible, pour les journaux. */
    public static final String TARGET = "forge-1.21.1";

    /** Fraction de tick écoulée (animations fluides). */
    public static float partialTick() {
        return net.minecraft.client.Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
    }
}
