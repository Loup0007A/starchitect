package com.starchitect.review;

/** Les huit rubriques de la fiche du critique. */
public enum Criterion {
    PALETTE, DETAIL, LIGHT, WINDOWS, ROOF, SYMMETRY, COMFORT, GARDEN;

    public String id() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }

    public String key() {
        return "starchitect.crit." + id();
    }
}
