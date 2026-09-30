package net.vg.combo.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.vg.combo.config.ComboConfig;

/**
 * A subtle black vignette that's present the whole time you're sneaking (a "stealth mode" cue),
 * with a brief red flash layered on top on a successful backstab. There's no gradient fill
 * available through {@code GuiGraphicsExtractor} (just flat {@code fill} rects), so both are
 * approximated with border strips whose thickness is a fraction of the screen size, rather than a
 * true smooth vignette.
 */
public final class ComboVignetteEffects {
    private static final int SNEAK_ALPHA = 45;
    private static final double SNEAK_THICKNESS_FRACTION = 0.07D;

    private static final long FLASH_DURATION_MILLIS = 350L;
    private static final int FLASH_MAX_ALPHA = 110;
    private static final int FLASH_RED = 0xB0;
    private static final double FLASH_THICKNESS_FRACTION = 0.10D;

    private static long flashTriggeredAtMillis = -1L;

    private ComboVignetteEffects() {
    }

    public static void trigger() {
        flashTriggeredAtMillis = System.currentTimeMillis();
    }

    public static void render(GuiGraphicsExtractor guiGraphics) {
        if (!ComboConfig.backstabVignetteEnabled) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        int width = guiGraphics.guiWidth();
        int height = guiGraphics.guiHeight();
        int minDimension = Math.min(width, height);

        if (minecraft.player != null && minecraft.player.isCrouching()) {
            int thickness = (int) (minDimension * SNEAK_THICKNESS_FRACTION);
            drawBorder(guiGraphics, width, height, thickness, SNEAK_ALPHA << 24);
        }

        if (flashTriggeredAtMillis < 0L) {
            return;
        }

        long elapsed = System.currentTimeMillis() - flashTriggeredAtMillis;
        if (elapsed >= FLASH_DURATION_MILLIS) {
            flashTriggeredAtMillis = -1L;
            return;
        }

        float fade = 1.0F - (float) elapsed / FLASH_DURATION_MILLIS;
        int alpha = Math.max(0, Math.min(255, Math.round(FLASH_MAX_ALPHA * fade)));
        int color = (alpha << 24) | (FLASH_RED << 16);
        int thickness = (int) (minDimension * FLASH_THICKNESS_FRACTION);
        drawBorder(guiGraphics, width, height, thickness, color);
    }

    private static void drawBorder(GuiGraphicsExtractor guiGraphics, int width, int height, int thickness, int color) {
        guiGraphics.fill(0, 0, width, thickness, color);
        guiGraphics.fill(0, height - thickness, width, height, color);
        guiGraphics.fill(0, 0, thickness, height, color);
        guiGraphics.fill(width - thickness, 0, width, height, color);
    }
}
