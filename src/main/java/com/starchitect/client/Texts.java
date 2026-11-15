package com.starchitect.client;

import com.starchitect.review.Critic;
import com.starchitect.review.Line;
import com.starchitect.review.Review;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/** Transforme les phrases enregistrées en texte affichable, dans la langue du moment. */
public final class Texts {

    private Texts() {}

    public static Component name(Review r) {
        if (r.customName != null && !r.customName.isBlank()) return Component.literal(r.customName);
        return Component.translatable("starchitect.name." + r.type, Component.translatable("starchitect.mat." + r.mat));
    }

    public static MutableComponent line(Line l, Review r) {
        Object[] args = new Object[l.args == null ? 0 : l.args.length];
        for (int i = 0; i < args.length; i++) {
            String a = l.args[i];
            if ("@name".equals(a)) args[i] = name(r);
            else if (a.startsWith("#")) args[i] = Component.translatable(a.substring(1));
            else args[i] = a;
        }
        return Component.translatable(l.key, args);
    }

    public static Component critic(Critic c) {
        return Component.translatable(c.nameKey());
    }

    /** « ★★☆ » */
    public static String stars(int stars) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < 3; i++) b.append(i < stars ? '★' : '☆');
        return b.toString();
    }

    public static Component starsLabel(Review r) {
        return Component.translatable(r.dirtHut ? "starchitect.stars.dirt" : "starchitect.stars." + r.stars);
    }
}
