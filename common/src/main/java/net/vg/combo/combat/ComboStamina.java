package net.vg.combo.combat;

import dev.architectury.networking.NetworkManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.vg.combo.config.ComboConfig;
import net.vg.combo.network.ComboStaminaPayload;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Server-authoritative stamina, drained by sprinting, blocking, and Full Sneak Mode's detection
 * bonus, and spent outright by a dodge. Sprinting/blocking are force-stopped the instant stamina
 * hits 0 (checked every tick from {@code Combo.init()}'s existing {@code TickEvent.PLAYER_POST}
 * loop); Full Sneak Mode isn't forced off (nothing to force — see {@code StealthVisibilityMixin}),
 * its bonus just stops applying until stamina recovers.
 */
public final class ComboStamina {
    private static final Map<UUID, Float> CURRENT = new HashMap<>();
    private static final Map<UUID, Long> LAST_DRAIN_TICK = new HashMap<>();

    private ComboStamina() {
    }

    public static void tick(ServerPlayer player, long gameTime) {
        UUID id = player.getUUID();
        float current = CURRENT.getOrDefault(id, (float) ComboConfig.staminaMax);
        boolean drained = false;

        if (player.isSprinting()) {
            if (current <= 0.0F) {
                player.setSprinting(false);
            } else {
                current = Math.max(0.0F, current - (float) ComboConfig.staminaSprintDrainPerTick);
                drained = true;
            }
        }

        if (player.isBlocking()) {
            if (current <= 0.0F) {
                player.stopUsingItem();
            } else {
                current = Math.max(0.0F, current - (float) ComboConfig.staminaBlockDrainPerTick);
                drained = true;
            }
        }

        if (ComboConfig.fullSneakModeEnabled && player.isDiscrete() && current > 0.0F) {
            current = Math.max(0.0F, current - (float) ComboConfig.staminaFullSneakDrainPerTick);
            drained = true;
        }

        if (drained) {
            LAST_DRAIN_TICK.put(id, gameTime);
        } else {
            long lastDrain = LAST_DRAIN_TICK.getOrDefault(id, gameTime);
            if (gameTime - lastDrain >= ComboConfig.staminaRegenDelayTicks) {
                current = Math.min((float) ComboConfig.staminaMax, current + (float) ComboConfig.staminaRegenPerTick);
            }
        }

        Float previous = CURRENT.put(id, current);
        if (previous == null || Math.abs(previous - current) > 0.001F) {
            NetworkManager.sendToPlayer(player, new ComboStaminaPayload(current, (float) ComboConfig.staminaMax));
        }
    }

    /** Whether Full Sneak Mode's detection bonus and silent takedowns are currently available. */
    public static boolean isFullSneakActive(Player player) {
        return ComboConfig.fullSneakModeEnabled && player.isDiscrete() && hasStamina(player);
    }

    public static boolean hasStamina(Player player) {
        return CURRENT.getOrDefault(player.getUUID(), (float) ComboConfig.staminaMax) > 0.0F;
    }

    /** Spends {@code amount} stamina if available, returning whether it was affordable. */
    public static boolean consume(ServerPlayer player, float amount) {
        UUID id = player.getUUID();
        float current = CURRENT.getOrDefault(id, (float) ComboConfig.staminaMax);
        if (current < amount) {
            return false;
        }

        current -= amount;
        CURRENT.put(id, current);
        LAST_DRAIN_TICK.put(id, player.level().getGameTime());
        NetworkManager.sendToPlayer(player, new ComboStaminaPayload(current, (float) ComboConfig.staminaMax));
        return true;
    }

    /**
     * Unconditionally spends {@code amount} stamina (clamped at 0) for a one-time "spike" cost —
     * a parry, finisher, or takedown still happens even on an empty bar, it just leaves nothing
     * left afterward. Use {@link #consume} instead when the action itself should be denied for
     * being unaffordable (a dodge).
     */
    public static void drainSpike(ServerPlayer player, float amount) {
        UUID id = player.getUUID();
        float current = CURRENT.getOrDefault(id, (float) ComboConfig.staminaMax);
        float updated = Math.max(0.0F, current - amount);
        CURRENT.put(id, updated);
        LAST_DRAIN_TICK.put(id, player.level().getGameTime());
        if (Math.abs(current - updated) > 0.001F) {
            NetworkManager.sendToPlayer(player, new ComboStaminaPayload(updated, (float) ComboConfig.staminaMax));
        }
    }

    public static void clear(Player player) {
        UUID id = player.getUUID();
        CURRENT.remove(id);
        LAST_DRAIN_TICK.remove(id);
    }
}
