package net.vg.combo.client;

import net.minecraft.client.Minecraft;
//? if >=26.1 {
import net.minecraft.gizmos.Gizmos;
//? }
import net.minecraft.world.entity.Entity;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Queues damage numbers received over the network and spawns them as world-space gizmo text
 * the next time a gizmo-collection frame runs. {@code Gizmos.*} calls are only valid inside an
 * active gizmo collector (set up by the frame that invokes {@link #drainAndSpawn()}), so the
 * network receiver can't call them directly.
 */
public final class ComboDamagePopups {
    private static final int FINISHER_COLOR = 0xFFFFAA00;
    private static final int NORMAL_COLOR = 0xFFFFFFFF;
    private static final float NORMAL_SCALE = 0.6F;
    private static final float FINISHER_SCALE = 0.9F;
    private static final int PERSIST_MILLIS = 900;
    private static final int MAX_STACK_LINES = 4;

    private static final Deque<Pending> QUEUE = new ArrayDeque<>();
    // Expiry timestamps of popups still visible per entity, so a flurry of hits stacks
    // vertically instead of every number landing in the exact same spot.
    private static final Map<Integer, List<Long>> ACTIVE_BY_ENTITY = new HashMap<>();

    private ComboDamagePopups() {
    }

    public static void enqueue(int entityId, float damage, boolean finisher) {
        String text = String.format("%.1f", damage);
        int color = finisher ? FINISHER_COLOR : NORMAL_COLOR;
        float scale = finisher ? FINISHER_SCALE : NORMAL_SCALE;
        QUEUE.add(new Pending(entityId, text, color, scale));
    }

    public static void drainAndSpawn() {
        if (QUEUE.isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            QUEUE.clear();
            return;
        }

        long now = System.currentTimeMillis();
        Pending pending;
        while ((pending = QUEUE.poll()) != null) {
            Entity entity = minecraft.level.getEntity(pending.entityId());
            if (entity == null) {
                continue;
            }

            List<Long> active = ACTIVE_BY_ENTITY.computeIfAbsent(pending.entityId(), id -> new ArrayList<>());
            active.removeIf(expiry -> expiry <= now);
            int line = Math.min(active.size(), MAX_STACK_LINES);
            active.add(now + PERSIST_MILLIS);

            //? if >=26.1 {
            Gizmos.billboardTextOverMob(entity, line, pending.text(), pending.color(), pending.scale())
                    .persistForMillis(PERSIST_MILLIS)
                    .fadeOut();
            //? } else {
            /*ComboWorldText.addPopup(entity, line, pending.text(), pending.color(), pending.scale(), PERSIST_MILLIS);
            *///? }
        }
    }

    private record Pending(int entityId, String text, int color, float scale) {
    }
}
