package net.vg.combo.combat;

import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.vg.combo.config.ComboConfig;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks when each player last started blocking, so a blocked hit (see the
 * {@code LivingEntity#blockedByItem} injection in {@code LivingEntityBlockMixin}) can be judged
 * as a just-in-time parry rather than an ordinary held block.
 */
public final class ComboParry {
    private static final Logger COMBO_LOGGER = LogUtils.getLogger();

    private static final Map<UUID, Long> BLOCK_START_TICK = new HashMap<>();
    private static final Map<UUID, Boolean> WAS_BLOCKING = new HashMap<>();

    private ComboParry() {
    }

    public static void trackBlockState(ServerPlayer player, long gameTime) {
        UUID id = player.getUUID();
        boolean blockingNow = player.isBlocking();
        boolean blockingBefore = WAS_BLOCKING.getOrDefault(id, Boolean.FALSE);
        if (blockingNow && !blockingBefore) {
            BLOCK_START_TICK.put(id, gameTime);
        }
        WAS_BLOCKING.put(id, blockingNow);
    }

    public static void onBlockedHit(ServerPlayer defender, LivingEntity attacker, ServerLevel level) {
        Long blockStart = BLOCK_START_TICK.get(defender.getUUID());
        long gameTime = level.getGameTime();

        if (blockStart == null || gameTime - blockStart > ComboConfig.parryWindowTicks) {
            return;
        }

        COMBO_LOGGER.info("combo-parry: PARRY SUCCESS defender={} attacker={}#{} delta={}",
                defender.getName().getString(), attacker.getClass().getSimpleName(), attacker.getId(),
                gameTime - blockStart);

        level.playSound(null, defender.getX(), defender.getY(), defender.getZ(),
                SoundEvents.TRIDENT_HIT, SoundSource.PLAYERS, 3.0F, 1.2F);

        double dx = defender.getX() - attacker.getX();
        double dz = defender.getZ() - attacker.getZ();
        double lengthSq = dx * dx + dz * dz;
        if (lengthSq > 1.0E-4) {
            double length = Math.sqrt(lengthSq);
            attacker.knockback(ComboConfig.parryKnockback, dx / length, dz / length);
        }

        ComboCombat.applyStagger(attacker, level);
        ComboCombat.armParryFinisher(defender, gameTime);
        ComboStamina.drainSpike(defender, (float) ComboConfig.staminaParrySpike);
    }

    public static void clear(ServerPlayer player) {
        UUID id = player.getUUID();
        BLOCK_START_TICK.remove(id);
        WAS_BLOCKING.remove(id);
    }
}
