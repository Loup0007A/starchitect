package com.starchitect.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.starchitect.Starchitect;
import com.starchitect.client.gui.AwardToast;
import com.starchitect.review.Critic;
import com.starchitect.review.Review;
import net.minecraft.client.Minecraft;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Les distinctions du monde (ou du serveur) : .minecraft/starchitect/&lt;monde&gt;.awards.json */
public final class Awards {

    private Awards() {}

    /** Toutes les distinctions, dans l'ordre d'affichage. */
    public static final List<String> ALL = List.of("first_review", "first_star", "three_stars", "perfect", "dirt_hut", "comeback",
        "all_critics", "styles", "collection", "night_owl");

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /** Ce qui est enregistré. */
    private static final class State {
        List<String> unlocked = new ArrayList<>();
        List<String> critics = new ArrayList<>();
        List<String> styles = new ArrayList<>();
    }

    private static String key;
    private static State state = new State();
    /** Distinctions à annoncer dès qu'aucun écran n'est ouvert. */
    private static final Deque<String> TOASTS = new ArrayDeque<>();

    private static void ensure() {
        String k = Guide.key();
        if (k.equals(key)) return;
        key = k;
        state = new State();
        Path f = file();
        if (!Files.exists(f)) return;
        try {
            State s = GSON.fromJson(Files.readString(f, StandardCharsets.UTF_8), State.class);
            if (s != null) {
                if (s.unlocked != null) state.unlocked = s.unlocked;
                if (s.critics != null) state.critics = s.critics;
                if (s.styles != null) state.styles = s.styles;
            }
        } catch (Exception e) {
            Starchitect.LOGGER.error("Starchitect : distinctions illisibles", e);
        }
    }

    private static Path file() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("starchitect").resolve(key + ".awards.json");
    }

    private static void save() {
        try {
            Files.createDirectories(file().getParent());
            Files.writeString(file(), GSON.toJson(state), StandardCharsets.UTF_8);
        } catch (Exception e) {
            Starchitect.LOGGER.error("Starchitect : distinctions non enregistrées", e);
        }
    }

    public static boolean has(String id) {
        ensure();
        return state.unlocked.contains(id);
    }

    public static int count() {
        ensure();
        return state.unlocked.size();
    }

    public static void reset() {
        ensure();
        state = new State();
        TOASTS.clear();
        save();
    }

    private static void unlock(String id) {
        if (state.unlocked.contains(id)) return;
        state.unlocked.add(id);
        TOASTS.add(id);
        Starchitect.LOGGER.info("Starchitect : distinction « {} »", id);
    }

    /** Après chaque critique publiée. */
    public static void onReview(Review r, boolean night) {
        ensure();
        if (!state.critics.contains(r.critic)) state.critics.add(r.critic);
        if (r.style != null && !state.styles.contains(r.style)) state.styles.add(r.style);
        unlock("first_review");
        if (r.stars >= 1) unlock("first_star");
        if (r.stars >= 3) unlock("three_stars");
        if (r.total >= 100) unlock("perfect");
        if (r.dirtHut) unlock("dirt_hut");
        if (r.previousStars >= 0 && r.stars > r.previousStars) unlock("comeback");
        if (state.critics.size() >= Critic.values().length) unlock("all_critics");
        if (state.styles.size() >= 4) unlock("styles");
        if (Guide.all().size() >= 5) unlock("collection");
        if (night) unlock("night_owl");
        save();
    }

    /** Annonce les distinctions en attente (quand aucun écran ne les cache). */
    static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (TOASTS.isEmpty() || mc.screen != null) return;
        String id = TOASTS.poll();
        mc.getToasts().addToast(new AwardToast(id));
    }

    public static Set<String> unlocked() {
        ensure();
        return new LinkedHashSet<>(state.unlocked);
    }
}
