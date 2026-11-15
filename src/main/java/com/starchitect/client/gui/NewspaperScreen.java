package com.starchitect.client.gui;

import com.starchitect.client.Guide;
import com.starchitect.client.Highlights;
import com.starchitect.client.Photos;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

import static com.starchitect.client.gui.Gfx.GOLD;
import static com.starchitect.client.gui.Gfx.GREY;
import static com.starchitect.client.gui.Gfx.INK;
import static com.starchitect.client.gui.Gfx.RULE;

/**
 * La critique, mise en page comme un article de journal : manchette, gros titre, sceau, lettrine,
 * citation en exergue, conseils du critique, photo de presse et fiche de notes.
 */
public class NewspaperScreen extends Screen {

    private final Review review;
    private final Screen parent;
    private final boolean fresh;
    private final long opened = System.nanoTime();
    private int shownStars;
    private boolean sealPlayed;
    private double scroll;
    private int contentHeight, viewHeight;

    // mise en page (calculée dans init)
    private int left, top, paperW, paperH, colW, sideX, sideW, bodyTop, bodyBottom;
    private final List<Block> blocks = new ArrayList<>();

    /** Un morceau de l'article : paragraphe (avec ou sans lettrine), exergue ou encadré de conseils. */
    private record Block(int kind, String cap, List<String> lines, List<List<String>> items) {
        static final int PARA = 0, QUOTE = 1, TIPS = 2;
    }

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

    /** Temps écoulé depuis l'ouverture, en ticks (indépendant de la pause du jeu). */
    private float now() {
        return (System.nanoTime() - opened) / 5e7f;
    }

    /** Moment où apparaît l'étoile n° i (1 à 3). */
    private float starTime(int i) {
        return 12 + i * 9;
    }

    private float sealTime() {
        return fresh ? starTime(review.stars) + 6 : -100;
    }

    /** Toute l'animation est jouée (étoiles et sceau). */
    public boolean revealed() {
        return shownStars >= review.stars && now() > sealTime() + 8;
    }

    @Override
    protected void init() {
        paperW = Math.min(width - 16, 480);
        paperH = Math.min(height - 10, 304);
        left = (width - paperW) / 2;
        top = (height - paperH) / 2;
        int inner = paperW - 24;
        sideW = Mth.clamp(inner * 36 / 100, 112, 168);
        colW = inner - sideW - 12;
        sideX = left + 12 + colW + 12;
        boolean show = review.darkSpots != null && review.darkSpots.length >= 3;
        int bw = 74, n = show ? 3 : 2;
        int bx = width / 2 - (n * bw + (n - 1) * 6) / 2, by = top + paperH - 24;
        addRenderableWidget(Button.builder(Component.translatable("starchitect.button.guide"),
            b -> minecraft.setScreen(new GuideScreen(this))).bounds(bx, by, bw, 18).build());
        if (show) {
            addRenderableWidget(Button.builder(Component.translatable("starchitect.button.show"), b -> {
                minecraft.setScreen(null);
                Highlights.show(review);
            }).bounds(bx + bw + 6, by, bw, 18).build());
        }
        addRenderableWidget(Button.builder(Component.translatable(parent == null ? "starchitect.button.close" : "starchitect.button.back"),
            b -> onClose()).bounds(bx + (n - 1) * (bw + 6), by, bw, 18).build());
        layoutArticle();
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    private void play(SoundEvent s, float pitch) {
        if (SConfig.SOUNDS.get()) minecraft.getSoundManager().play(SimpleSoundInstance.forUI(s, pitch));
    }

    @Override
    public void tick() {
        float t = now();
        while (fresh && shownStars < review.stars && t >= starTime(shownStars + 1)) {
            shownStars++;
            play(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.8f + shownStars * 0.2f);
            if (shownStars == 3) play(SoundEvents.PLAYER_LEVELUP, 1f);
        }
        if (fresh && !sealPlayed && t >= sealTime() + 6) {
            sealPlayed = true;
            play(SoundEvents.BOOK_PUT, 0.7f);
            if (review.stars == 0) play(SoundEvents.VILLAGER_NO, 0.9f);
        }
    }

    /** Petit glissement vers le haut à l'ouverture. */
    private float slide() {
        float t = Math.min(1f, now() / 7f);
        return (1 - t) * (1 - t) * 30f;
    }

    //#if mc1201
    //$$ @Override
    //$$ public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
    //$$     renderBackground(g);
    //$$     g.pose().pushPose();
    //$$     g.pose().translate(0, slide(), 0);
    //$$     paper(g);
    //$$     super.render(g, mouseX, mouseY, partial);
    //$$     g.pose().popPose();
    //$$ }
    //$$
    //$$ @Override
    //$$ public boolean mouseScrolled(double x, double y, double delta) {
    //$$     return scrollBy(delta);
    //$$ }
    //#else
    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        g.pose().pushPose();
        g.pose().translate(0, slide(), 0);
        super.render(g, mouseX, mouseY, partial);
        g.pose().popPose();
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partial) {
        g.pose().pushPose();
        g.pose().translate(0, -slide(), 0);
        super.renderBackground(g, mouseX, mouseY, partial);
        g.pose().popPose();
        paper(g);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double dx, double dy) {
        return scrollBy(dy);
    }
    //#endif

    public boolean scrollBy(double delta) {
        int max = Math.max(0, contentHeight - viewHeight);
        scroll = Mth.clamp(scroll - delta * 14, 0, max);
        return true;
    }

    // ───────────────────────── mise en page de l'article ─────────────────────────

    private int capWidth(String cap) {
        return font.width(Component.literal(cap).withStyle(ChatFormatting.BOLD)) * 3 + 4;
    }

    private void layoutArticle() {
        blocks.clear();
        List<String> paragraphs = new ArrayList<>();
        StringBuilder cur = null;
        for (Line l : review.body) {
            if (l.isBreak()) {
                if (cur != null) paragraphs.add(cur.toString());
                cur = null;
                continue;
            }
            String s = Texts.line(l, review).getString();
            cur = cur == null ? new StringBuilder(s) : cur.append(' ').append(s);
        }
        if (cur != null) paragraphs.add(cur.toString());

        String quote = review.quote >= 0 && review.quote < review.body.size()
            ? Texts.line(review.body.get(review.quote), review).getString() : null;
        for (int p = 0; p < paragraphs.size(); p++) {
            String text = paragraphs.get(p);
            if (p == 0 && !text.isEmpty() && Character.isLetter(text.codePointAt(0))) {
                int n = Character.charCount(text.codePointAt(0));
                String cap = text.substring(0, n).toUpperCase();
                int capW = capWidth(cap);
                blocks.add(new Block(Block.PARA, cap, Gfx.wrap(font, text.substring(n), i -> i < 3 ? colW - capW : colW), null));
            } else {
                blocks.add(new Block(Block.PARA, null, Gfx.wrap(font, text, i -> colW), null));
            }
            if (p == 1 && quote != null) {
                blocks.add(new Block(Block.QUOTE, null, Gfx.wrap(font, "« " + quote + " »", i -> colW - 30), null));
                quote = null;
            }
        }
        if (review.tips != null && !review.tips.isEmpty()) {
            List<List<String>> items = new ArrayList<>();
            for (Line t : review.tips) items.add(Gfx.wrap(font, Texts.line(t, review).getString(), i -> colW - 24));
            blocks.add(new Block(Block.TIPS, null, null, items));
        }
    }

    private int height(Block b) {
        return switch (b.kind()) {
            case Block.PARA -> Math.max(b.lines().size(), b.cap() != null ? 3 : 0) * 10 + 6;
            case Block.QUOTE -> b.lines().size() * 10 + 16;
            default -> {
                int h = 22;
                for (List<String> item : b.items()) h += item.size() * 10 + 3;
                yield h + 6;
            }
        };
    }

    private void drawBlock(GuiGraphics g, Block b, int x, int y, Critic critic) {
        Font f = font;
        switch (b.kind()) {
            case Block.PARA -> {
                int capW = 0;
                if (b.cap() != null) {
                    Gfx.big(g, f, Component.literal(b.cap()).withStyle(ChatFormatting.BOLD), x, y - 1, 3f, critic.color);
                    capW = capWidth(b.cap());
                }
                int ly = y;
                for (int i = 0; i < b.lines().size(); i++) {
                    g.drawString(f, b.lines().get(i), x + (i < 3 ? capW : 0), ly, INK, false);
                    ly += 10;
                }
            }
            case Block.QUOTE -> {
                g.hLine(x + 24, x + colW - 24, y + 1, critic.color);
                int ly = y + 6;
                for (String s : b.lines()) {
                    Component c = Component.literal(s).withStyle(ChatFormatting.ITALIC);
                    g.drawString(f, c, x + (colW - f.width(c)) / 2, ly, critic.color, false);
                    ly += 10;
                }
                g.hLine(x + 24, x + colW - 24, ly + 2, critic.color);
            }
            default -> {
                int h = height(b) - 8;
                g.fill(x, y, x + colW, y + h, 0x18000000);
                g.fill(x, y, x + 2, y + h, critic.color);
                g.drawString(f, Component.translatable("starchitect.tips").withStyle(ChatFormatting.BOLD), x + 8, y + 5, INK, false);
                int ly = y + 18;
                for (List<String> item : b.items()) {
                    g.drawString(f, "✎", x + 8, ly, critic.color, false);
                    for (String s : item) {
                        g.drawString(f, s, x + 19, ly, INK, false);
                        ly += 10;
                    }
                    ly += 3;
                }
            }
        }
    }

    // ───────────────────────── dessin ─────────────────────────

    private void paper(GuiGraphics g) {
        Font f = font;
        Critic critic = review.criticOrDefault();
        int right = left + paperW, bottom = top + paperH;
        Gfx.paper(g, left, top, paperW, paperH, false);
        Gfx.frame(g, left, top, paperW, paperH);
        float t = now();

        // manchette
        Component mast = Component.translatable("starchitect.paper").withStyle(ChatFormatting.BOLD);
        float scale = Math.min(2.4f, (paperW - 150f) / f.width(mast));
        int mastW = (int) (f.width(mast) * scale);
        int mx = left + (paperW - mastW) / 2;
        Gfx.big(g, f, mast, mx, top + 9, scale, INK);
        int dw = Math.min(56, (paperW - mastW) / 2 - 18);
        if (dw > 20) {
            int dy = top + 9 + (int) (4.5f * scale);
            Gfx.divider(g, mx - 8 - dw / 2, dy, dw);
            Gfx.divider(g, mx + mastW + 8 + dw / 2, dy, dw);
        }
        int y = top + 9 + (int) (9 * scale) + 3;
        g.hLine(left + 8, right - 9, y, INK);
        g.hLine(left + 8, right - 9, y + 2, INK);
        Component sub = Component.translatable("starchitect.paper.sub", review.day, Math.max(1, Guide.count()));
        Component motto = Component.translatable("starchitect.paper.motto").withStyle(ChatFormatting.ITALIC);
        g.drawString(f, sub, left + 11, y + 5, GREY, false);
        g.drawString(f, motto, right - 11 - f.width(motto), y + 5, GREY, false);
        y += 16;
        g.hLine(left + 8, right - 9, y, RULE);
        y += 6;

        // gros titre, étoiles, sceau
        int sealSize = 46;
        int headW = paperW - 24 - sealSize - 8;
        MutableComponent headline = Texts.line(review.headline, review).withStyle(ChatFormatting.BOLD);
        int headTop = y;
        if (f.width(headline) * 1.35f <= headW) {
            Gfx.big(g, f, headline, left + 12, y, 1.35f, INK);
            y += 15;
        } else {
            List<FormattedCharSequence> hl = f.split(headline, headW);
            for (int i = 0; i < Math.min(2, hl.size()); i++) {
                g.drawString(f, hl.get(i), left + 12, y, INK, false);
                y += 10;
            }
            y += 2;
        }
        int popIndex = -1;
        float pop = -1;
        if (fresh && shownStars > 0 && t < starTime(shownStars) + 6) {
            popIndex = shownStars - 1;
            pop = (t - starTime(shownStars)) / 6f;
        }
        Gfx.stars(g, left + 12, y, 12, shownStars, popIndex, pop);
        if (!fresh || t > starTime(review.stars) + 2) {
            g.drawString(f, Component.literal("— ").append(Texts.starsLabel(review)).withStyle(ChatFormatting.ITALIC),
                left + 12 + 3 * 14 + 4, y + 2, review.dirtHut ? 0xFF7A5230 : GOLD, false);
        }
        y += 16;
        float sp = (t - sealTime()) / 6f;
        if (sp > 0) {
            float k = Math.min(1f, sp);
            float s = sealSize * (1f + 1.6f * (1 - k) * (1 - k));
            float cx = right - 12 - sealSize / 2f, cy = headTop + sealSize / 2f - 4;
            Gfx.image(g, Gfx.seal(review.stars, review.dirtHut), cx - s / 2, cy - s / 2, s, s, 96, 96, Math.min(1f, k * 1.6f));
        }
        g.hLine(left + 8, right - 9 - sealSize - 6, y, RULE);
        bodyTop = y + 6;
        bodyBottom = bottom - 30;

        // article (défilant)
        viewHeight = bodyBottom - bodyTop;
        contentHeight = 0;
        for (Block b : blocks) contentHeight += height(b);
        int max = Math.max(0, contentHeight - viewHeight);
        scroll = Mth.clamp(scroll, 0, max);
        g.enableScissor(left + 8, bodyTop, left + 12 + colW + 2, bodyBottom);
        int by = bodyTop - (int) scroll;
        for (Block b : blocks) {
            int h = height(b);
            if (by + h >= bodyTop && by <= bodyBottom) drawBlock(g, b, left + 12, by, critic);
            by += h;
        }
        g.disableScissor();
        if (max > 0) {
            int knob = Math.max(12, viewHeight * viewHeight / contentHeight);
            int ky = bodyTop + (int) ((viewHeight - knob) * (scroll / max));
            int sx = left + 12 + colW + 4;
            g.fill(sx, bodyTop, sx + 1, bodyBottom, 0x30000000);
            g.fill(sx - 1, ky, sx + 2, ky + knob, critic.color);
            if (scroll < max) {
                Component more = Component.translatable("starchitect.paper.scroll").withStyle(ChatFormatting.ITALIC);
                g.drawString(f, more, left + 12, bodyBottom + 2, GREY, false);
            }
        }

        // colonne de droite : photo, signature, fiche
        g.vLine(sideX - 6, bodyTop - 2, bodyBottom, RULE);
        int sy = bodyTop;
        int ph = sideW * 9 / 16;
        ResourceLocation photo = Photos.texture(review.photo);
        g.fill(sideX + 1, sy + 1, sideX + sideW + 3, sy + ph + 3, 0x40000000);
        if (photo != null) Gfx.image(g, photo, sideX, sy, sideW, ph, Photos.W, Photos.H);
        else Gfx.image(g, Gfx.NO_PHOTO, sideX, sy, sideW, ph, 128, 72);
        g.renderOutline(sideX - 1, sy - 1, sideW + 2, ph + 2, INK);
        sy += ph + 2;
        Component cap = Component.translatable(photo != null ? "starchitect.photo.caption" : "starchitect.photo.none").withStyle(ChatFormatting.ITALIC);
        small(g, cap, sideX, sy + 1, GREY);
        sy += 10;
        Portraits.draw(g, critic, sideX + 2, sy + 2, 2);
        int nx = sideX + 32;
        List<FormattedCharSequence> nm = f.split(Texts.critic(critic).copy().withStyle(ChatFormatting.BOLD), sideW - 32);
        int ny = sy + 2;
        for (int i = 0; i < Math.min(2, nm.size()); i++) {
            g.drawString(f, nm.get(i), nx, ny, INK, false);
            ny += 9;
        }
        small(g, Component.translatable(critic.nameKey() + ".title").withStyle(ChatFormatting.ITALIC), nx, ny, GREY);
        sy = Math.max(sy + 30, ny + 10);
        g.drawString(f, Component.translatable("starchitect.paper.sheet").withStyle(ChatFormatting.BOLD), sideX, sy, INK, false);
        Component total = Component.translatable("starchitect.paper.score", review.total);
        g.drawString(f, total, sideX + sideW - f.width(total), sy, critic.color, false);
        sy += 11;
        int barX = sideX + sideW / 2 + 2, barW = sideW - (barX - sideX) - 12;
        for (Criterion c : Criterion.values()) {
            if (sy + 8 > bodyBottom) break;
            int v = review.scores != null && c.ordinal() < review.scores.length ? review.scores[c.ordinal()] : 0;
            small(g, Component.translatable(c.key()), sideX, sy, INK);
            // les barres se remplissent à l'ouverture
            float grow = fresh ? Math.min(1f, Math.max(0f, (t - 4 - c.ordinal()) / 8f)) : 1f;
            g.fill(barX, sy + 1, barX + barW, sy + 6, 0xFFDCD1B8);
            int col = v >= 8 ? 0xFF3E8E41 : v >= 5 ? 0xFFC08A1E : 0xFFB23A3A;
            int fw = (int) (barW * v / 10f * grow);
            g.fillGradient(barX, sy + 1, barX + fw, sy + 6, col, darker(col));
            for (int k = 1; k < 5; k++) g.fill(barX + barW * k / 5, sy + 1, barX + barW * k / 5 + 1, sy + 6, 0x22000000);
            small(g, Component.literal(String.valueOf(v)), barX + barW + 3, sy, GREY);
            sy += 9;
        }
        Component sig = Component.literal("— ").append(Texts.critic(critic)).withStyle(ChatFormatting.ITALIC);
        g.drawString(f, sig, right - 12 - f.width(sig), bodyBottom + 2, GREY, false);
    }

    private void small(GuiGraphics g, Component c, int x, int y, int color) {
        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale(0.75f, 0.75f, 1f);
        g.drawString(font, c, 0, 0, color, false);
        g.pose().popPose();
    }

    static int darker(int c) {
        int r = (c >> 16 & 0xFF) * 3 / 4, gg = (c >> 8 & 0xFF) * 3 / 4, b = (c & 0xFF) * 3 / 4;
        return 0xFF000000 | r << 16 | gg << 8 | b;
    }
}
