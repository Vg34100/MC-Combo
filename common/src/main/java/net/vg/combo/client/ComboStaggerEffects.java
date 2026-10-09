package net.vg.combo.client;

import net.minecraft.client.Minecraft;
//? if >=26.1 {
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.TextGizmo;
//? }
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Tracks currently-staggered entities and, every gizmo-collection frame, draws a small ring of
 * stars orbiting each one's head. Unlike {@link ComboDamagePopups} this issues a fresh gizmo call
 * every frame (rather than one persisted call) since the orbit position needs to keep moving for
 * the duration of the stagger, not just fade out.
 */
public final class ComboStaggerEffects {
    private static final int STAR_COUNT = 3;
    private static final double ORBIT_RADIUS = 0.6D;
    private static final double ORBIT_PERIOD_MILLIS = 1500.0D;
    private static final int STAR_COLOR = 0xFFFFD700;
    private static final float STAR_SCALE = 0.5F;

    private static final Map<Integer, Long> ACTIVE = new HashMap<>();

    private ComboStaggerEffects() {
    }

    public static void register(int entityId, int durationTicks) {
        ACTIVE.put(entityId, System.currentTimeMillis() + durationTicks * 50L);
    }

    public static void drainAndRender() {
        if (ACTIVE.isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            ACTIVE.clear();
            return;
        }

        long now = System.currentTimeMillis();
        Iterator<Map.Entry<Integer, Long>> iterator = ACTIVE.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Long> entry = iterator.next();
            if (entry.getValue() <= now) {
                iterator.remove();
                continue;
            }

            Entity entity = minecraft.level.getEntity(entry.getKey());
            if (entity == null) {
                iterator.remove();
                continue;
            }

            renderStars(entity, now);
        }
    }

    private static void renderStars(Entity entity, long now) {
        double baseAngle = (now % (long) ORBIT_PERIOD_MILLIS) / ORBIT_PERIOD_MILLIS * Math.PI * 2.0D;
        double headY = entity.getY() + entity.getBbHeight() + 0.3D;
        double centerX = entity.getX();
        double centerZ = entity.getZ();

        //? if >=26.1 {
        TextGizmo.Style style = TextGizmo.Style.forColorAndCentered(STAR_COLOR).withScale(STAR_SCALE);
        //? }
        for (int i = 0; i < STAR_COUNT; i++) {
            double angle = baseAngle + (Math.PI * 2.0D / STAR_COUNT) * i;
            double x = centerX + Math.cos(angle) * ORBIT_RADIUS;
            double z = centerZ + Math.sin(angle) * ORBIT_RADIUS;
            //? if >=26.1 {
            Gizmos.billboardText("✦", new Vec3(x, headY, z), style).setAlwaysOnTop();
            //? } else {
            /*ComboWorldText.draw("✦", new Vec3(x, headY, z), STAR_COLOR, STAR_SCALE);
            *///? }
        }
    }
}
