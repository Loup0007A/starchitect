package com.starchitect.client.gui;

import com.starchitect.client.ClientBoot;
import com.starchitect.client.Guide;
import com.starchitect.client.Photos;
import com.starchitect.client.Texts;
import com.starchitect.review.Review;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import java.util.List;

import static com.starchitect.client.gui.Gfx.GOLD;
import static com.starchitect.client.gui.Gfx.GREY;
import static com.starchitect.client.gui.Gfx.INK;
import static com.starchitect.client.gui.Gfx.RULE;

/** Le guide : toutes les constructions critiquées dans ce monde, en fiches avec leur photo. */
public class GuideScreen extends Screen {

    private static final int ROW = 42;

    private final Screen parent;
    private double scroll;
    private int left, top, paperW, paperH, listTop, listBottom;
    private int mx, my;

    public GuideScreen(Screen parent) {
        super(Component.translatable("starchitect.guide.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        paperW = Math.min(width - 16, 380);
        paperH = Math.min(height - 10, 290);
        left = (width - paperW) / 2;
        top = (height - paperH) / 2;
        addRenderableWidget(Button.builder(Component.translatable(parent == null ? "starchitect.button.close" : "starchitect.button.back"),
            b -> onClose()).bounds(width / 2 - 40, top + paperH - 24, 80, 18).build());
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    //#if mc1201
    //$$ @Override
    //$$ public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
    //$$     renderBackground(g);
    //$$     mx = mouseX;
    //$$     my = mouseY;
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
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        mx = mouseX;
        my = mouseY;
        super.render(g, mouseX, mouseY, partial);
    }

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

    private boolean scrollBy(double delta) {
        int max = Math.max(0, Guide.all().size() * ROW - (listBottom - listTop));
        scroll = Mth.clamp(scroll - delta * 14, 0, max);
        return true;
    }

    private int rowAt(double x, double y) {
        if (x < left + 8 || x > left + paperW - 8 || y < listTop || y >= listBottom) return -1;
        int i = (int) ((y - listTop + scroll) / ROW);
        return i >= 0 && i < Guide.all().size() ? i : -1;
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        int i = rowAt(x, y);
        if (button == 0 && i >= 0) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1f));
            minecraft.setScreen(new NewspaperScreen(Guide.all().get(i), this, false));
            return true;
        }
        return super.mouseClicked(x, y, button);
    }

    private void paper(GuiGraphics g) {
        Font f = font;
        int right = left + paperW, bottom = top + paperH;
        Gfx.paper(g, left, top, paperW, paperH, false);
        Gfx.frame(g, left, top, paperW, paperH);

        Component title = Component.translatable("starchitect.guide.title").withStyle(ChatFormatting.BOLD);
        float scale = Math.min(1.7f, (paperW - 120f) / f.width(title));
        int tw = (int) (f.width(title) * scale);
        int tx = left + (paperW - tw) / 2;
        Gfx.big(g, f, title, tx, top + 10, scale, INK);
        int dw = Math.min(46, (paperW - tw) / 2 - 16);
        if (dw > 18) {
            Gfx.divider(g, tx - 8 - dw / 2, top + 10 + (int) (4.5f * scale), dw);
            Gfx.divider(g, tx + tw + 8 + dw / 2, top + 10 + (int) (4.5f * scale), dw);
        }
        int y = top + 12 + (int) (9 * scale) + 4;
        g.hLine(left + 8, right - 9, y, INK);
        g.hLine(left + 8, right - 9, y + 2, INK);
        List<Review> all = Guide.all();
        int rank = Guide.rank();
        // titre du joueur, avec une rosette
        Gfx.image(g, Gfx.seal(Math.min(3, Math.max(1, rank - 1)), false), left + 12, y + 5, 24, 24, 96, 96, rank == 0 ? 0.35f : 1f);
        Component rk = Component.translatable("starchitect.guide.rank",
            Component.translatable("starchitect.rank." + rank).withStyle(ChatFormatting.BOLD));
        g.drawString(f, rk, left + 40, y + 7, INK, false);
        Component summary = Component.translatable("starchitect.guide.summary", all.size(), Guide.totalStars());
        g.drawString(f, summary, left + 40, y + 18, GREY, false);
        if (rank + 1 < Guide.RANKS.length) {
            Component next = Component.translatable("starchitect.guide.next", Guide.RANKS[rank + 1]).withStyle(ChatFormatting.ITALIC);
            g.drawString(f, next, right - 12 - f.width(next), y + 7, GREY, false);
            // progression vers le titre suivant
            int lo = Guide.RANKS[rank], hi = Guide.RANKS[rank + 1];
            int pw = Math.min(90, f.width(next));
            int px = right - 12 - pw;
            float p = (Guide.totalStars() - lo) / (float) Math.max(1, hi - lo);
            g.fill(px, y + 19, px + pw, y + 23, 0xFFDCD1B8);
            g.fillGradient(px, y + 19, px + (int) (pw * p), y + 23, 0xFFE2B33A, 0xFFB07C14);
        }
        y += 33;
        g.hLine(left + 8, right - 9, y, RULE);
        // étagère des distinctions
        java.util.Set<String> got = com.starchitect.client.Awards.unlocked();
        Component lbl = Component.translatable("starchitect.awards").withStyle(ChatFormatting.BOLD);
        g.drawString(f, lbl, left + 12, y + 8, INK, false);
        Component cnt = Component.translatable("starchitect.awards.count", got.size(), com.starchitect.client.Awards.ALL.size());
        g.drawString(f, cnt, left + 12, y + 18, GREY, false);
        int ax = left + 18 + Math.max(f.width(lbl), f.width(cnt));
        int step = Math.min(22, (right - 12 - ax) / com.starchitect.client.Awards.ALL.size());
        for (String id : com.starchitect.client.Awards.ALL) {
            boolean on = got.contains(id);
            if (!on) g.fill(ax + 2, y + 6, ax + step - 2, y + 24, 0x14000000);
            Gfx.image(g, AwardToast.seal(id), ax, y + 4, step, step, 96, 96, on ? 1f : 0.22f);
            if (mx >= ax && mx < ax + step && my >= y + 4 && my < y + 4 + step) {
                List<FormattedCharSequence> tip = new java.util.ArrayList<>();
                tip.add(Component.translatable("starchitect.award." + id).withStyle(on ? ChatFormatting.GOLD : ChatFormatting.GRAY).getVisualOrderText());
                tip.add(Component.translatable("starchitect.award." + id + ".desc").getVisualOrderText());
                if (!on) tip.add(Component.translatable("starchitect.award.locked").withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_GRAY).getVisualOrderText());
                setTooltipForNextRenderPass(tip);
            }
            ax += step;
        }
        y += 28;
        g.hLine(left + 8, right - 9, y, RULE);
        listTop = y + 3;
        listBottom = bottom - 30;

        if (all.isEmpty()) {
            Gfx.image(g, Gfx.NO_PHOTO, left + (paperW - 128) / 2f, listTop + 10, 128, 72, 128, 72, 0.8f);
            Component a = Component.translatable("starchitect.guide.empty").withStyle(ChatFormatting.ITALIC);
            Component b = Component.translatable("starchitect.guide.empty2", ClientBoot.KEY_INVITE.getTranslatedKeyMessage());
            g.drawString(f, a, left + (paperW - f.width(a)) / 2, listTop + 90, GREY, false);
            g.drawString(f, b, left + (paperW - f.width(b)) / 2, listTop + 102, GREY, false);
            return;
        }
        int max = Math.max(0, all.size() * ROW - (listBottom - listTop));
        scroll = Mth.clamp(scroll, 0, max);
        int hover = rowAt(mx, my);
        g.enableScissor(left + 6, listTop, right - 6, listBottom);
        for (int i = 0; i < all.size(); i++) {
            int ry = listTop + i * ROW - (int) scroll;
            if (ry + ROW < listTop || ry > listBottom) continue;
            Review r = all.get(i);
            int cx = left + 10, cw = paperW - 20 - (max > 0 ? 6 : 0);
            if (i == hover) g.fill(cx, ry + 1, cx + cw, ry + ROW - 2, 0x1A6B2D5C);
            // vignette : la photo de presse
            int thW = 56, thH = 32, thX = cx + 3, thY = ry + 4;
            g.fill(thX + 1, thY + 1, thX + thW + 2, thY + thH + 2, 0x40000000);
            ResourceLocation photo = Photos.texture(r.photo);
            if (photo != null) Gfx.image(g, photo, thX, thY, thW, thH, Photos.W, Photos.H);
            else Gfx.image(g, Gfx.NO_PHOTO, thX, thY, thW, thH, 128, 72);
            g.renderOutline(thX - 1, thY - 1, thW + 2, thH + 2, INK);
            // sceau miniature sur la vignette
            Gfx.image(g, Gfx.seal(r.stars, r.dirtHut), thX + thW - 12, thY + thH - 14, 16, 16, 96, 96);
            int textX = thX + thW + 8;
            int starsX = cx + cw - 3 - 3 * 12;
            List<FormattedCharSequence> nm = f.split(Texts.name(r).copy().withStyle(ChatFormatting.BOLD), starsX - textX - 4);
            g.drawString(f, nm.get(0), textX, ry + 5, INK, false);
            Gfx.stars(g, starsX, ry + 4, 10, r.stars, -1, -1);
            Component info = Component.translatable("starchitect.guide.entry", r.day, Texts.critic(r.criticOrDefault()), r.x, r.z);
            List<FormattedCharSequence> il = f.split(info, cx + cw - textX - 4);
            g.drawString(f, il.get(0), textX, ry + 16, GREY, false);
            if (r.headline != null) {
                Component hl = Texts.line(r.headline, r).withStyle(ChatFormatting.ITALIC);
                g.pose().pushPose();
                g.pose().translate(textX, ry + 27, 0);
                g.pose().scale(0.75f, 0.75f, 1f);
                List<FormattedCharSequence> hls = f.split(hl, (int) ((cx + cw - textX - 4) / 0.75f));
                g.drawString(f, hls.get(0), 0, 0, r.dirtHut ? 0xFF7A5230 : GOLD, false);
                g.pose().popPose();
            }
            g.hLine(cx + 2, cx + cw - 2, ry + ROW - 1, 0x30000000);
        }
        g.disableScissor();
        if (max > 0) {
            int view = listBottom - listTop;
            int content = all.size() * ROW;
            int knob = Math.max(12, view * view / content);
            int ky = listTop + (int) ((view - knob) * (scroll / max));
            g.fill(right - 11, listTop, right - 10, listBottom, 0x30000000);
            g.fill(right - 12, ky, right - 9, ky + knob, RULE);
        }
    }
}
