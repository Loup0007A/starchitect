package com.starchitect.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.starchitect.Starchitect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * La photo de presse : une image du monde prise juste après le rendu des blocs (sans la main ni le HUD),
 * développée en sépia avec grain et vignettage, puis enregistrée dans .minecraft/starchitect/photos.
 */
@Mod.EventBusSubscriber(modid = Starchitect.MOD_ID, value = Dist.CLIENT)
public final class Photos {

    private Photos() {}

    public static final int W = 256, H = 144;

    private static boolean requested;
    /** Dernière photo prise, en attente de publication. */
    private static NativeImage pending;
    private static final Map<String, ResourceLocation> CACHE = new HashMap<>();

    /** La photo sera prise à la prochaine image. */
    public static void request() {
        requested = true;
        discard();
    }

    public static void discard() {
        if (pending != null) pending.close();
        pending = null;
    }

    public static boolean hasPending() {
        return pending != null;
    }

    @SubscribeEvent
    public static void onLevelStage(RenderLevelStageEvent event) {
        if (!requested || event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) return;
        requested = false;
        try (NativeImage shot = Screenshot.takeScreenshot(Minecraft.getInstance().getMainRenderTarget())) {
            pending = develop(shot);
            Visits.flash();
        } catch (Throwable t) {
            Starchitect.LOGGER.error("Starchitect : photo impossible", t);
        }
    }

    private static int clamp(double v) {
        return v < 0 ? 0 : v > 255 ? 255 : (int) v;
    }

    /** Recadrage 16:9, réduction (moyenne des pixels), sépia, contraste, grain et vignettage. */
    static NativeImage develop(NativeImage src) {
        int sw = src.getWidth(), sh = src.getHeight();
        int cw = sw, ch = sw * 9 / 16;
        if (ch > sh) {
            ch = sh;
            cw = sh * 16 / 9;
        }
        int ox = (sw - cw) / 2, oy = (sh - ch) / 2;
        NativeImage out = new NativeImage(W, H, false);
        Random rnd = new Random(1234);
        for (int y = 0; y < H; y++) {
            int y0 = oy + y * ch / H, y1 = Math.max(y0 + 1, oy + (y + 1) * ch / H);
            for (int x = 0; x < W; x++) {
                int x0 = ox + x * cw / W, x1 = Math.max(x0 + 1, ox + (x + 1) * cw / W);
                long r = 0, g = 0, b = 0;
                int n = 0;
                for (int yy = y0; yy < y1; yy += 2) {
                    for (int xx = x0; xx < x1; xx += 2) {
                        int p = src.getPixelRGBA(xx, yy); // ABGR
                        r += p & 0xFF;
                        g += (p >> 8) & 0xFF;
                        b += (p >> 16) & 0xFF;
                        n++;
                    }
                }
                double lum = (0.30 * r + 0.59 * g + 0.11 * b) / Math.max(1, n);
                // contraste en S, comme un tirage argentique
                double t = lum / 255.0;
                t = t * t * (3 - 2 * t) * 0.75 + t * 0.25;
                lum = t * 235 + 12;
                double dx = (x - W / 2.0) / (W / 2.0), dy = (y - H / 2.0) / (H / 2.0);
                double vignette = 1.0 - 0.38 * Math.pow(Math.min(1.0, (dx * dx + dy * dy) / 1.6), 1.3);
                lum = lum * vignette + rnd.nextGaussian() * 6;
                int R = clamp(lum * 1.08 + 14), G = clamp(lum * 0.95 + 6), B = clamp(lum * 0.76);
                out.setPixelRGBA(x, y, 0xFF000000 | (B << 16) | (G << 8) | R);
            }
        }
        return out;
    }

    private static Path dir() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("starchitect").resolve("photos");
    }

    private static ResourceLocation register(String uid, NativeImage img) {
        ResourceLocation loc = Starchitect.id("photo/" + uid);
        Minecraft.getInstance().getTextureManager().register(loc, new DynamicTexture(img));
        CACHE.put(uid, loc);
        return loc;
    }

    /** Publie la photo en attente sous l'identifiant de la critique ; renvoie false s'il n'y en a pas. */
    public static boolean publish(String uid) {
        if (pending == null) return false;
        NativeImage img = pending;
        pending = null;
        try {
            Files.createDirectories(dir());
            img.writeToFile(dir().resolve(uid + ".png"));
        } catch (Exception e) {
            Starchitect.LOGGER.error("Starchitect : photo non enregistrée", e);
        }
        register(uid, img);
        return true;
    }

    /** La texture de la photo d'une critique (chargée depuis le disque au besoin), ou null. */
    public static ResourceLocation texture(String uid) {
        if (uid == null || !uid.matches("[a-z0-9]+")) return null;
        if (CACHE.containsKey(uid)) return CACHE.get(uid);
        Path f = dir().resolve(uid + ".png");
        if (!Files.exists(f)) {
            CACHE.put(uid, null);
            return null;
        }
        try (InputStream in = Files.newInputStream(f)) {
            return register(uid, NativeImage.read(in));
        } catch (Exception e) {
            Starchitect.LOGGER.error("Starchitect : photo illisible ({})", f, e);
            CACHE.put(uid, null);
            return null;
        }
    }

    public static void delete(String uid) {
        if (uid == null || !uid.matches("[a-z0-9]+")) return;
        ResourceLocation loc = CACHE.remove(uid);
        if (loc != null) Minecraft.getInstance().getTextureManager().release(loc);
        try {
            Files.deleteIfExists(dir().resolve(uid + ".png"));
        } catch (Exception ignored) {
        }
    }
}
