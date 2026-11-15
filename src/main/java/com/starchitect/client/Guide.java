package com.starchitect.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.starchitect.Starchitect;
import com.starchitect.review.Review;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/** Le guide : toutes les critiques d'un monde (ou d'un serveur), dans .minecraft/starchitect/&lt;monde&gt;.json */
public final class Guide {

    private Guide() {}

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static String key;
    private static List<Review> reviews = new ArrayList<>();

    /** Paliers de titres, en étoiles cumulées. */
    public static final int[] RANKS = {0, 1, 4, 8, 15, 25};

    private static String currentKey() {
        Minecraft mc = Minecraft.getInstance();
        MinecraftServer sp = mc.getSingleplayerServer();
        String k;
        if (sp != null) {
            Path root = sp.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize();
            k = "world_" + (root.getFileName() == null ? "world" : root.getFileName().toString());
        } else {
            ServerData d = mc.getCurrentServer();
            k = "server_" + (d == null ? "unknown" : d.ip);
        }
        return k.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    /** Identifiant du monde ou du serveur courant (nom des fichiers). */
    public static String key() {
        ensure();
        return key;
    }

    private static Path file(String k) {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("starchitect").resolve(k + ".json");
    }

    private static void ensure() {
        String k = currentKey();
        if (k.equals(key)) return;
        key = k;
        reviews = new ArrayList<>();
        Path f = file(k);
        if (!Files.exists(f)) return;
        try {
            List<Review> list = GSON.fromJson(Files.readString(f, StandardCharsets.UTF_8), new TypeToken<List<Review>>() {}.getType());
            if (list != null) {
                for (Review r : list) {
                    if (r == null || r.dim == null || r.body == null || r.scores == null) continue;
                    // critiques écrites avant la 1.1
                    if (r.tips == null) r.tips = new ArrayList<>();
                    if (r.darkSpots == null) r.darkSpots = new int[0];
                    reviews.add(r);
                }
            }
        } catch (Exception e) {
            Starchitect.LOGGER.error("Starchitect : guide illisible ({})", f, e);
        }
    }

    public static List<Review> all() {
        ensure();
        return reviews;
    }

    /** La critique la plus proche, à moins de {@code radius} blocs (ou null). */
    public static Review near(String dim, int x, int z, int radius) {
        Review best = null;
        double bestD = (double) radius * radius;
        for (Review r : all()) {
            double d = r.distanceSq(dim, x, z);
            if (d <= bestD) {
                bestD = d;
                best = r;
            }
        }
        return best;
    }

    /** Publie une critique (elle remplace celle de la visite précédente). */
    public static void publish(Review r, Review previous) {
        ensure();
        if (previous != null) {
            reviews.remove(previous);
            // la photo d'avant reste (avant / après) ; celle d'avant-avant part
            if (previous.previousPhoto != null && !previous.previousPhoto.equals(r.previousPhoto)) Photos.delete(previous.previousPhoto);
        }
        reviews.add(0, r);
        save();
    }

    public static void remove(Review r) {
        ensure();
        reviews.remove(r);
        Photos.delete(r.photo);
        Photos.delete(r.previousPhoto);
        save();
    }

    public static void reset() {
        ensure();
        for (Review r : reviews) {
            Photos.delete(r.photo);
            Photos.delete(r.previousPhoto);
        }
        reviews.clear();
        save();
    }

    public static int totalStars() {
        int n = 0;
        for (Review r : all()) n += r.stars;
        return n;
    }

    public static int rank() {
        int stars = totalStars();
        int r = 0;
        for (int i = 0; i < RANKS.length; i++) if (stars >= RANKS[i]) r = i;
        return r;
    }

    /** Nombre de critiques déjà écrites dans ce monde (pour le numéro de la critique). */
    public static int count() {
        int n = 0;
        for (Review r : all()) n += r.visits;
        return n;
    }

    public static void save() {
        if (key == null) return;
        try {
            Path f = file(key);
            Files.createDirectories(f.getParent());
            Path tmp = f.resolveSibling(key + ".json.tmp");
            Files.writeString(tmp, GSON.toJson(reviews), StandardCharsets.UTF_8);
            Files.move(tmp, f, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (Exception e) {
            Starchitect.LOGGER.error("Starchitect : impossible d'enregistrer le guide", e);
        }
    }
}
