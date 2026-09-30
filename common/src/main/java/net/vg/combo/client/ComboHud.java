package net.vg.combo.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.vg.combo.combat.ComboCombat;

/**
 * Shared HUD renderer. Matches both Fabric's {@code HudElement.extractRenderState} and
 * NeoForge's {@code GuiLayer.render} functional interface shape ({@code (GuiGraphicsExtractor, DeltaTracker) -> void}),
 * so both loaders wire this same method in directly with no per-loader drawing code.
 */
public final class ComboHud {
    private static final int PIP_SIZE = 8;
    private static final int PIP_GAP = 4;
    private static final int FILLED_COLOR = 0xFFFFFFFF;
    private static final int FINISHER_COLOR = 0xFFFFAA00;
    private static final int EMPTY_COLOR = 0x66FFFFFF;

    private static final int STAMINA_BAR_WIDTH = 80;
    private static final int STAMINA_BAR_HEIGHT = 4;
    private static final int STAMINA_EMPTY_COLOR = 0x88222222;
    private static final int STAMINA_FILLED_COLOR = 0xFFE8D34D;

    // The stamina bar sits where the pips used to be (68), and the pips move up further by the
    // same gap (10) that used to separate the two rows — keeps the pip/stamina relationship
    // constant while pushing the whole block clear of vanilla's hotbar item-name/armor-bar area.
    private static final int STAMINA_BAR_Y_OFFSET = 68;
    private static final int GAP_BETWEEN_ROWS = 10;
    private static final int PIP_Y_OFFSET = STAMINA_BAR_Y_OFFSET + GAP_BETWEEN_ROWS;

    private ComboHud() {
    }

    public static void render(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
        ComboVignetteEffects.render(guiGraphics);
        renderStaminaBar(guiGraphics);

        int stage = ComboClientState.stage();
        if (stage <= 0) {
            return;
        }

        int length = ComboCombat.COMBO_LENGTH;
        int totalWidth = length * PIP_SIZE + (length - 1) * PIP_GAP;
        int startX = guiGraphics.guiWidth() / 2 - totalWidth / 2;
        int y = guiGraphics.guiHeight() - PIP_Y_OFFSET;

        boolean finisher = ComboClientState.finisher();
        for (int i = 0; i < length; i++) {
            int x = startX + i * (PIP_SIZE + PIP_GAP);
            int color = i < stage ? (finisher ? FINISHER_COLOR : FILLED_COLOR) : EMPTY_COLOR;
            guiGraphics.fill(x, y, x + PIP_SIZE, y + PIP_SIZE, color);
        }
    }

    private static void renderStaminaBar(GuiGraphicsExtractor guiGraphics) {
        float max = ComboStaminaClient.max();
        float current = ComboStaminaClient.current();
        if (max <= 0.0F || current >= max - 0.01F) {
            return;
        }

        int x = guiGraphics.guiWidth() / 2 - STAMINA_BAR_WIDTH / 2;
        int y = guiGraphics.guiHeight() - STAMINA_BAR_Y_OFFSET;

        guiGraphics.fill(x, y, x + STAMINA_BAR_WIDTH, y + STAMINA_BAR_HEIGHT, STAMINA_EMPTY_COLOR);
        int filledWidth = Math.round(STAMINA_BAR_WIDTH * (current / max));
        if (filledWidth > 0) {
            guiGraphics.fill(x, y, x + filledWidth, y + STAMINA_BAR_HEIGHT, STAMINA_FILLED_COLOR);
        }
    }
}
