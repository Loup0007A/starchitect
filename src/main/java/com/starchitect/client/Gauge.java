package com.starchitect.client;

import com.starchitect.Starchitect;
import com.starchitect.analysis.Analyzer;
import com.starchitect.analysis.Survey;
import com.starchitect.client.gui.Gfx;
import com.starchitect.config.SConfig;
import com.starchitect.review.Critic;
import com.starchitect.review.Criterion;
import com.starchitect.review.Scoring;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * Le mètre de chantier (touche G) : une estimation de la note de la construction où l'on se trouve,
 * remesurée en continu pendant que l'on bâtit. Le relevé est étalé sur une seconde et demie.
 */
public final class Gauge {

    private Gauge() {}

    private static boolean active;
    private static Scanner scanner;
    private static int wait;
    private static Survey survey;
    private static Scoring scoring;
    private static Critic critic = Critic.AUGUSTIN;

    public static boolean active() {
        return active;
    }

    public static void toggle() {
        active = !active;
        scanner = null;
        survey = null;
        scoring = null;
        wait = 0;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.displayClientMessage(active
                ? Component.translatable("starchitect.gauge.on", ClientBoot.KEY_GAUGE.getTranslatedKeyMessage())
                : Component.translatable("starchitect.gauge.off"), true);
        }
    }

    static void tick() {
        if (!active) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            active = false;
            return;
        }
        if (scanner == null) {
            if (--wait > 0) return;
            int r = SConfig.SCAN_RADIUS.get();
            scanner = new Scanner(mc.level, mc.player.blockPosition(), r, 10, Math.max(16, r));
            Critic fixed = Visits.forced != null ? Visits.forced : Critic.byId(SConfig.CRITIC.get().trim());
            critic = fixed != null ? fixed : Critic.AUGUSTIN;
        }
        int cols = scanner.grid.sx * scanner.grid.sz;
        scanner.step(cols / 30 + 1, 0);
        if (scanner.done()) {
            try {
                survey = Analyzer.analyze(scanner.grid, scanner.cx, scanner.cz, 12);
                scoring = survey.blocks >= SConfig.MIN_BLOCKS.get() ? Scoring.of(survey, critic) : null;
            } catch (Exception e) {
                Starchitect.LOGGER.error("Starchitect : mètre de chantier", e);
            }
            scanner = null;
            wait = 30;
        }
    }

    static void render(GuiGraphics g) {
        if (!active) return;
        Minecraft mc = Minecraft.getInstance();
        Font f = mc.font;
        int w = 132, h = scoring == null ? 34 : 122;
        int x = g.guiWidth() - w - 6, y = 6;
        Gfx.paper(g, x, y, w, h, true);
        g.fill(x, y, x + w, y + 2, critic.color);
        Component title = Component.translatable("starchitect.gauge.title").withStyle(ChatFormatting.BOLD);
        g.drawString(f, title, x + (w - f.width(title)) / 2, y + 6, Gfx.INK, false);
        Gfx.divider(g, x + w / 2, y + 18, w - 20);
        if (scoring == null) {
            Component c = Component.translatable(survey == null ? "starchitect.gauge.measuring" : "starchitect.gauge.none")
                .withStyle(ChatFormatting.ITALIC);
            g.drawString(f, c, x + (w - f.width(c)) / 2, y + 23, Gfx.GREY, false);
            return;
        }
        int cy = y + 24;
        Gfx.stars(g, x + 6, cy, 10, scoring.stars, -1, -1);
        Component est = Component.translatable("starchitect.gauge.estimate", scoring.total);
        g.drawString(f, est, x + w - 6 - f.width(est), cy + 1, critic.color, false);
        cy += 14;
        Criterion weakest = Criterion.PALETTE;
        for (Criterion c : Criterion.values()) {
            double v = scoring.score(c);
            if (v * critic.weight(weakest) < scoring.score(weakest) * critic.weight(c)) weakest = c;
            g.pose().pushPose();
            g.pose().translate(x + 6, cy, 0);
            g.pose().scale(0.75f, 0.75f, 1f);
            g.drawString(f, Component.translatable(c.key()), 0, 0, Gfx.INK, false);
            g.pose().popPose();
            int bx = x + 62, bw = w - 68;
            g.fill(bx, cy + 1, bx + bw, cy + 5, 0xFFD8CDB4);
            int col = v >= 8 ? 0xFF3E8E41 : v >= 5 ? 0xFFC08A1E : 0xFFB23A3A;
            g.fill(bx, cy + 1, bx + (int) (bw * v / 10), cy + 5, col);
            cy += 8;
        }
        cy += 3;
        Component weak = Component.translatable("starchitect.gauge.weak", Component.translatable(weakest.key()).withStyle(ChatFormatting.BOLD));
        g.pose().pushPose();
        g.pose().translate(x + 6, cy, 0);
        g.pose().scale(0.75f, 0.75f, 1f);
        g.drawString(f, weak, 0, 0, 0xFFB23A3A, false);
        g.drawString(f, Texts.critic(critic).copy().withStyle(ChatFormatting.ITALIC), 0, 11, Gfx.GREY, false);
        g.pose().popPose();
    }
}
