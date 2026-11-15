package com.starchitect.review;

import java.util.ArrayList;
import java.util.List;

/** Une critique publiée dans le guide (enregistrée telle quelle en JSON). */
public final class Review {

    public String uid;
    /** Dimension (identifiant) et centre de la construction. */
    public String dim;
    public int x, y, z;
    /** Jour du monde où la critique a été écrite. */
    public long day;
    public long time;
    public String critic;
    /** Nom donné par le critique : type (cabanon, villa…) + matériau, ou nom choisi par le joueur. */
    public String type, mat;
    public String customName;
    public int stars;
    public int total;
    public int[] scores;
    public List<Line> body = new ArrayList<>();
    /** Indice dans {@link #body} de la phrase mise en exergue (-1 = aucune). */
    public int quote = -1;
    public Line headline;
    /** Étoiles de la visite précédente (-1 = première visite). */
    public int previousStars = -1;
    public int visits = 1;
    public long fingerprint;
    public int blocks;
    public boolean dirtHut;
    /** Photo de presse (identifiant du fichier dans .minecraft/starchitect/photos), ou null. */
    public String photo;
    /** Les conseils du critique. */
    public List<Line> tips = new ArrayList<>();
    /** Coins dans le noir (x, y, z à la suite), pour les montrer dans le monde. */
    public int[] darkSpots = new int[0];

    public Critic criticOrDefault() {
        Critic c = Critic.byId(critic);
        return c == null ? Critic.AUGUSTIN : c;
    }

    public double distanceSq(String dim, int x, int z) {
        if (!this.dim.equals(dim)) return Double.MAX_VALUE;
        double dx = this.x - x, dz = this.z - z;
        return dx * dx + dz * dz;
    }
}
