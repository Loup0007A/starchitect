package com.starchitect.review;

/**
 * Une phrase de critique : une clé de traduction et ses arguments. Un argument qui commence par « # »
 * est lui-même une clé de traduction (nom d'un bloc…). Les critiques sont enregistrées ainsi, pas en
 * texte : elles se relisent dans la langue du moment.
 */
public final class Line {

    /** Clé spéciale : fin de paragraphe. */
    public static final String BREAK = "\n";

    public String key;
    public String[] args;

    public Line() {}

    public Line(String key, Object... args) {
        this.key = key;
        this.args = new String[args.length];
        for (int i = 0; i < args.length; i++) this.args[i] = String.valueOf(args[i]);
    }

    public boolean isBreak() {
        return BREAK.equals(key);
    }
}
