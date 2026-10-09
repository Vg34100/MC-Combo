package net.vg.combo.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/** Legacy billboard renderer for the same damage numbers and orbiting stagger stars. */
public final class ComboWorldText {
    private static final List<Popup> POPUPS = new ArrayList<>();
    private static ClientLevel lastLevel;
    private static PoseStack framePoses;
    private static Camera frameCamera;
    private static MultiBufferSource frameBuffers;

    private ComboWorldText() {
    }

    public static void addPopup(Entity entity, int row, String text, int color, float scale, int durationMillis) {
        // Matches Gizmos.billboardTextOverMob's fixed position and row spacing.
        Vec3 position = new Vec3(entity.getBlockX() + 0.5D,
                entity.getY() + 2.4D + row * 0.25D, entity.getBlockZ() + 0.5D);
        POPUPS.add(new Popup(text, position, color, scale,
                System.currentTimeMillis() + durationMillis, durationMillis));
    }

    public static void renderFrame(PoseStack poses, Camera camera, MultiBufferSource buffers) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != lastLevel) {
            POPUPS.clear();
            lastLevel = minecraft.level;
        }
        if (minecraft.level == null || poses == null || buffers == null) {
            return;
        }
        framePoses = poses;
        frameCamera = camera;
        frameBuffers = buffers;
        try {
            ComboDamagePopups.drainAndSpawn();
            long now = System.currentTimeMillis();
            POPUPS.removeIf(popup -> popup.expiresAt() <= now);
            for (Popup popup : POPUPS) {
                float fade = (float) (popup.expiresAt() - now) / popup.durationMillis();
                int alpha = Math.max(0, Math.min(255, Math.round((popup.color() >>> 24) * fade)));
                draw(popup.text(), popup.position(), (popup.color() & 0xFFFFFF) | alpha << 24, popup.scale(), false);
            }
            ComboStaggerEffects.drainAndRender();
        } finally {
            framePoses = null;
            frameCamera = null;
            frameBuffers = null;
        }
    }

    public static void draw(String text, Vec3 position, int color, float scale) {
        draw(text, position, color, scale, true);
    }

    private static void draw(String text, Vec3 position, int color, float scale, boolean centered) {
        if (framePoses == null) {
            return;
        }
        Font font = Minecraft.getInstance().font;
        Vec3 relative = position.subtract(frameCamera.getPosition());
        framePoses.pushPose();
        try {
            framePoses.translate(relative.x, relative.y, relative.z);
            framePoses.mulPose(frameCamera.rotation());
            // Match DrawableGizmoPrimitives' text units, handedness and alignment.
            float worldScale = scale / 16.0F;
            framePoses.scale(worldScale, -worldScale, worldScale);
            float fontX = centered ? -font.width(text) / 2.0F : -0.5F / scale;
            font.drawInBatch(text, fontX, 0.0F, color, false,
                    framePoses.last().pose(), frameBuffers, Font.DisplayMode.SEE_THROUGH,
                    0, LightTexture.FULL_BRIGHT);
        } finally {
            framePoses.popPose();
        }
    }

    private record Popup(String text, Vec3 position, int color, float scale, long expiresAt, int durationMillis) {
    }
}
