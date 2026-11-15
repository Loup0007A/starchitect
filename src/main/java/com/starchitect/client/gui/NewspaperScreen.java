package com.starchitect.client.gui;

import com.starchitect.client.Guide;
import com.starchitect.client.Texts;
import com.starchitect.config.SConfig;
import com.starchitect.review.Critic;
import com.starchitect.review.Criterion;
import com.starchitect.review.Line;
import com.starchitect.review.Review;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

/** La critique, mise en page comme un article de journal. */
public class NewspaperScreen extends Screen {

    public static final int PAPER = 0xFFF3EBD9;
    public static final int INK = 0xFF2B2520;
    public static final int GREY = 0xFF6E6252;
    public static final int RULE = 0xFF8C7F6A;
    public static final int GOLD = 0xFFC08A1E;

    private final Review review;
    private final Screen parent;
    private final boolean fresh;
    private int ticks;
    private int shownStars;
    private double scroll;
    private int contentHeight, viewHeight;

    // mise en page (calculée dans init)
    private int left, top, paperW, paperH, colW, sideX, sideW, bodyTop, bodyBottom;

    public NewspaperScreen(Review review, Screen parent, boolean fresh) {
        super(Component.translatable("starchitect.paper"));
        this.review = review;
        this.parent = parent;
        this.fresh = fresh;
        this.shownStars = fresh ? 0 : review.stars;
    }

    public Review review() {
        return review;
    }

    /** Toutes les étoiles sont affichées (fin de l'animation). */
    public boolean revealed() {
        return shownStars >= review.stars && (!fresh || ticks > 14 + review.stars * 10);
    }

    @Override
    protected void init() {
        paperW = Math.min(width - 16, 440);
        paperH = Math.min(height - 12, 270);
        left = (width - paperW) / 2;
        top = (height - paperH) / 2;
        int inner = paperW - 24;
        sideW = Mth.clamp(inner * 34 / 100, 110, 150);
        colW = inner - sideW - 10;
        sideX = left + 12 + colW + 10;
        int bw = 70;
        addRenderableWidget(Button.builder(Component.translatable("starchitect.button.guide"),
            b -> minecraft.setScreen(new GuideScreen(this))).bounds(width / 2 - bw - 4, top + paperH - 24, bw, 18).build());
        addRenderableWidget(Button.builder(Component.translatable(parent == null ? "starchitect.button.close" : "starchitect.button.back"),
            b -> onClose()).bounds(width / 2 + 4, top + paperH - 24, bw, 18).build());
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    public void tick() {
        ticks++;
        if (fresh && shownStars < review.stars && ticks >= 14 + (shownStars + 1) * 10) {
            shownStars++;
            if (SConfig.SOUNDS.get()) {
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.8f + shownStars * 0.2f));
                if (shownStars == 3) minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.PLAYER_LEVELUP, 1f));
            }
        }
        if (fresh && ticks == 14 && review.stars == 0 && SConfig.SOUNDS.get()) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.VILLAGER_NO, 0.9f));
        }
    }

    //#if mc1201
    //$$ @Override
    //$$ public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
    //$$     renderBackground(g);
    //$$     paper(g);
    //$$     super.render(g, mouseX, mouseY, partial);
    //$$ }
    //$$
    //$$ @Override
    //$$ public boolean mouseScrolled(double x, double y, double delta) {
    //$$     return scrollBy(delta);
    //$$ }
    //#else
    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partial) {
        super.renderBackground(g, mouseX, mouseY, partial);
        paper(g);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double dx, double dy) {
        return scrollBy(dy);
    }
    //#endif

    public boolean scrollBy(double delta) {
        int max = Math.max(0, contentHeight - viewHeight);
        scroll = Mth.clamp(scroll - delta * 12, 0, max);
        return true;
    }

    private void paper(GuiGraphics g) {
        Font f = font;
        Critic critic = review.criticOrDefault();
        int right = left + paperW, bottom = top + paperH;
        // ombre, papier, cadre
        g.fill(left + 4, top + 4, right + 4, bottom + 4, 0x66000000);
        g.fill(left, top, right, bottom, PAPER);
        g.renderOutline(left, top, paperW, paperH, INK);
        g.renderOutline(left + 3, top + 3, paperW - 6, paperH - 6, RULE);

        // titre du journal
        Component mast = Component.translatable("starchitect.paper").withStyle(ChatFormatting.BOLD);
        float scale = 2f;
        int mastW = (int) (f.width(mast) * scale);
        if (mastW > paperW - 30) {
            scale = (paperW - 30f) / f.width(mast);
            mastW = (int) (f.width(mast) * scale);
        }
        g.pose().pushPose();
        g.pose().translate(left + (paperW - mastW) / 2f, top + 9, 0);
        g.pose().scale(scale, scale, 1);
        g.drawString(f, mast, 0, 0, INK, false);
        g.pose().popPose();
        int y = top + 9 + (int) (9 * scale) + 3;
        g.hLine(left + 8, right - 9, y, INK);
        g.hLine(left + 8, right - 9, y + 2, INK);
        Component sub = Component.translatable("starchitect.paper.sub", review.day, Math.max(1, Guide.count()));
        Component motto = Component.translatable("starchitect.paper.motto").withStyle(ChatFormatting.ITALIC);
        g.drawString(f, sub, left + 10, y + 5, GREY, false);
        g.drawString(f, motto, right - 10 - f.width(motto), y + 5, GREY, false);
        y += 16;
        g.hLine(left + 8, right - 9, y, RULE);
        y += 5;

        // gros titre et étoiles
        MutableComponent headline = Texts.line(review.headline, review).withStyle(ChatFormatting.BOLD);
        List<FormattedCharSequence> hl = f.split(headline, paperW - 24);
        for (int i = 0; i < Math.min(2, hl.size()); i++) {
            g.drawString(f, hl.get(i), left + 12, y, INK, false);
            y += 10;
        }
        String stars = Texts.stars(shownStars);
        g.drawString(f, Component.literal(stars).withStyle(ChatFormatting.BOLD), left + 12, y + 1, GOLD, false);
        if (revealed()) {
            g.drawString(f, Component.literal("— ").append(Texts.starsLabel(review)).withStyle(ChatFormatting.ITALIC),
                left + 14 + f.width(stars) + 4, y + 1, review.dirtHut ? 0xFF7A5230 : GOLD, false);
        }
        y += 13;
        g.hLine(left + 8, right - 9, y, RULE);
        bodyTop = y + 5;
        bodyBottom = bottom - 30;

        // corps de l'article (défilant)
        viewHeight = bodyBottom - bodyTop;
        List<List<FormattedCharSequence>> paragraphs = paragraphs(colW);
        contentHeight = 0;
        for (List<FormattedCharSequence> p : paragraphs) contentHeight += p.size() * 10 + 5;
        int max = Math.max(0, contentHeight - viewHeight);
        scroll = Mth.clamp(scroll, 0, max);
        g.enableScissor(left + 8, bodyTop, left + 12 + colW + 2, bodyBottom);
        int by = bodyTop - (int) scroll;
        for (List<FormattedCharSequence> p : paragraphs) {
            for (FormattedCharSequence line : p) {
                g.drawString(f, line, left + 12, by, INK, false);
                by += 10;
            }
            by += 5;
        }
        g.disableScissor();
        if (max > 0) {
            // barre de défilement
            int trackH = viewHeight;
            int knob = Math.max(12, trackH * viewHeight / contentHeight);
            int ky = bodyTop + (int) ((trackH - knob) * (scroll / max));
            g.fill(left + 12 + colW + 3, bodyTop, left + 12 + colW + 4, bodyBottom, 0x40000000);
            g.fill(left + 12 + colW + 2, ky, left + 12 + colW + 5, ky + knob, RULE);
            if (scroll < max) {
                Component more = Component.translatable("starchitect.paper.scroll").withStyle(ChatFormatting.ITALIC);
                g.drawString(f, more, left + 12, bodyBottom + 2, GREY, false);
            }
        }

        // colonne de droite : le critique et sa fiche
        g.vLine(sideX - 6, bodyTop - 2, bodyBottom, RULE);
        int sy = bodyTop;
        Portraits.draw(g, critic, sideX + 2, sy + 2, 2);
        int nx = sideX + 32;
        List<FormattedCharSequence> nm = f.split(Texts.critic(critic).copy().withStyle(ChatFormatting.BOLD), sideW - 32);
        int ny = sy + 1;
        for (int i = 0; i < Math.min(2, nm.size()); i++) {
            g.drawString(f, nm.get(i), nx, ny, INK, false);
            ny += 9;
        }
        List<FormattedCharSequence> ttl = f.split(Component.translatable(critic.nameKey() + ".title").withStyle(ChatFormatting.ITALIC), sideW - 32);
        for (int i = 0; i < Math.min(2, ttl.size()); i++) {
            g.drawString(f, ttl.get(i), nx, ny, GREY, false);
            ny += 9;
        }
        sy = Math.max(sy + 30, ny + 2);
        g.drawString(f, Component.translatable("starchitect.paper.sheet").withStyle(ChatFormatting.BOLD), sideX, sy, INK, false);
        Component total = Component.translatable("starchitect.paper.score", review.total);
        g.drawString(f, total, sideX + sideW - f.width(total), sy, critic.color, false);
        sy += 11;
        int barX = sideX + sideW - 46;
        for (Criterion c : Criterion.values()) {
            if (sy + 9 > bodyBottom) break;
            int v = review.scores != null && c.ordinal() < review.scores.length ? review.scores[c.ordinal()] : 0;
            g.drawString(f, Component.translatable(c.key()), sideX, sy, INK, false);
            g.fill(barX, sy + 2, barX + 40, sy + 6, 0xFFD8CDB4);
            int col = v >= 8 ? 0xFF3E8E41 : v >= 5 ? 0xFFC08A1E : 0xFFB23A3A;
            g.fill(barX, sy + 2, barX + v * 4, sy + 6, col);
            sy += 10;
        }
        // phrase en exergue
        if (review.quote >= 0 && review.quote < review.body.size()) {
            sy += 4;
            MutableComponent q = Component.literal("« ").append(Texts.line(review.body.get(review.quote), review)).append(" »")
                .withStyle(ChatFormatting.ITALIC);
            List<FormattedCharSequence> ql = f.split(q, sideW - 8);
            int lines = Math.min(ql.size(), Math.max(0, (bodyBottom - sy - 4) / 9));
            if (lines >= 2) {
                g.fill(sideX, sy - 2, sideX + 2, sy + lines * 9, critic.color);
                for (int i = 0; i < lines; i++) {
                    FormattedCharSequence s = ql.get(i);
                    g.drawString(f, s, sideX + 6, sy, GREY, false);
                    sy += 9;
                }
            }
        }
        // signature
        Component sig = Component.literal("— ").append(Texts.critic(critic)).withStyle(ChatFormatting.ITALIC);
        g.drawString(f, sig, right - 12 - f.width(sig), bodyBottom + 2, GREY, false);
    }

    private List<List<FormattedCharSequence>> paragraphs(int w) {
        List<List<FormattedCharSequence>> out = new ArrayList<>();
        MutableComponent cur = null;
        for (Line l : review.body) {
            if (l.isBreak()) {
                if (cur != null) out.add(font.split(cur, w));
                cur = null;
                continue;
            }
            MutableComponent c = Texts.line(l, review);
            cur = cur == null ? Component.empty().append(c) : cur.append(" ").append(c);
        }
        if (cur != null) out.add(font.split(cur, w));
        return out;
    }
}
