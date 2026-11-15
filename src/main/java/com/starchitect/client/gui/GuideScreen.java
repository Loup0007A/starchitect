package com.starchitect.client.gui;

import com.starchitect.client.ClientBoot;
import com.starchitect.client.Guide;
import com.starchitect.client.Texts;
import com.starchitect.review.Review;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import java.util.List;

import static com.starchitect.client.gui.NewspaperScreen.GOLD;
import static com.starchitect.client.gui.NewspaperScreen.GREY;
import static com.starchitect.client.gui.NewspaperScreen.INK;
import static com.starchitect.client.gui.NewspaperScreen.PAPER;
import static com.starchitect.client.gui.NewspaperScreen.RULE;

/** Le guide : toutes les constructions critiquées dans ce monde, avec leurs étoiles. */
public class GuideScreen extends Screen {

    private static final int ROW = 24;

    private final Screen parent;
    private double scroll;
    private int left, top, paperW, paperH, listTop, listBottom;

    public GuideScreen(Screen parent) {
        super(Component.translatable("starchitect.guide.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        paperW = Math.min(width - 16, 360);
        paperH = Math.min(height - 12, 260);
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
    //$$     paper(g, mouseX, mouseY);
    //$$     super.render(g, mouseX, mouseY, partial);
    //$$ }
    //$$
    //$$ @Override
    //$$ public boolean mouseScrolled(double x, double y, double delta) {
    //$$     return scrollBy(delta);
    //$$ }
    //#else
    private int mx, my;

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        mx = mouseX;
        my = mouseY;
        super.render(g, mouseX, mouseY, partial);
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partial) {
        super.renderBackground(g, mouseX, mouseY, partial);
        paper(g, mx, my);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double dx, double dy) {
        return scrollBy(dy);
    }
    //#endif

    private boolean scrollBy(double delta) {
        int max = Math.max(0, Guide.all().size() * ROW - (listBottom - listTop));
        scroll = Mth.clamp(scroll - delta * 12, 0, max);
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

    private void paper(GuiGraphics g, int mouseX, int mouseY) {
        Font f = font;
        int right = left + paperW, bottom = top + paperH;
        g.fill(left + 4, top + 4, right + 4, bottom + 4, 0x66000000);
        g.fill(left, top, right, bottom, PAPER);
        g.renderOutline(left, top, paperW, paperH, INK);
        g.renderOutline(left + 3, top + 3, paperW - 6, paperH - 6, RULE);

        Component title = Component.translatable("starchitect.guide.title").withStyle(ChatFormatting.BOLD);
        g.pose().pushPose();
        float scale = 1.5f;
        g.pose().translate(left + (paperW - f.width(title) * scale) / 2f, top + 9, 0);
        g.pose().scale(scale, scale, 1);
        g.drawString(f, title, 0, 0, INK, false);
        g.pose().popPose();
        int y = top + 25;
        g.hLine(left + 8, right - 9, y, INK);
        List<Review> all = Guide.all();
        int rank = Guide.rank();
        Component summary = Component.translatable("starchitect.guide.summary", all.size(), Guide.totalStars());
        g.drawString(f, summary, left + 12, y + 5, GREY, false);
        Component rk = Component.translatable("starchitect.guide.rank",
            Component.translatable("starchitect.rank." + rank).withStyle(ChatFormatting.BOLD));
        g.drawString(f, rk, right - 12 - f.width(rk), y + 5, INK, false);
        if (rank + 1 < Guide.RANKS.length) {
            Component next = Component.translatable("starchitect.guide.next", Guide.RANKS[rank + 1]).withStyle(ChatFormatting.ITALIC);
            g.drawString(f, next, right - 12 - f.width(next), y + 15, GREY, false);
        }
        y += 27;
        g.hLine(left + 8, right - 9, y, RULE);
        listTop = y + 3;
        listBottom = bottom - 30;

        if (all.isEmpty()) {
            Component a = Component.translatable("starchitect.guide.empty").withStyle(ChatFormatting.ITALIC);
            Component b = Component.translatable("starchitect.guide.empty2", ClientBoot.KEY_INVITE.getTranslatedKeyMessage());
            g.drawString(f, a, (width - f.width(a)) / 2, listTop + 30, GREY, false);
            g.drawString(f, b, (width - f.width(b)) / 2, listTop + 42, GREY, false);
            return;
        }
        int max = Math.max(0, all.size() * ROW - (listBottom - listTop));
        scroll = Mth.clamp(scroll, 0, max);
        int hover = rowAt(mouseX, mouseY);
        g.enableScissor(left + 6, listTop, right - 6, listBottom);
        for (int i = 0; i < all.size(); i++) {
            int ry = listTop + i * ROW - (int) scroll;
            if (ry + ROW < listTop || ry > listBottom) continue;
            Review r = all.get(i);
            if (i == hover) g.fill(left + 8, ry, right - 8, ry + ROW - 1, 0x22000000);
            Portraits.draw(g, r.criticOrDefault(), left + 12, ry + 3, 1);
            String stars = Texts.stars(r.stars);
            int sx = right - 14 - f.width(stars);
            List<FormattedCharSequence> nm = f.split(Texts.name(r).copy().withStyle(ChatFormatting.BOLD), sx - left - 36);
            g.drawString(f, nm.get(0), left + 30, ry + 3, INK, false);
            g.drawString(f, stars, sx, ry + 3, r.dirtHut ? 0xFF7A5230 : GOLD, false);
            Component info = Component.translatable("starchitect.guide.entry", r.day, Texts.critic(r.criticOrDefault()), r.x, r.z);
            List<FormattedCharSequence> il = f.split(info, right - left - 46);
            g.drawString(f, il.get(0), left + 30, ry + 13, GREY, false);
            g.hLine(left + 10, right - 11, ry + ROW - 1, 0x30000000);
        }
        g.disableScissor();
    }
}
