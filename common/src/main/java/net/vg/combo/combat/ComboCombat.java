package net.vg.combo.combat;

import dev.architectury.networking.NetworkManager;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.vg.combo.config.ComboConfig;
import net.vg.combo.network.ComboBackstabPayload;
import net.vg.combo.network.ComboDamagePopupPayload;
import net.vg.combo.network.ComboStaggerPayload;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Server-authoritative melee combo tracking. All calls happen on the server game thread
 * from the {@code Player.attack} redirect, so no external synchronization is needed here.
 */
public final class ComboCombat {
    // Structural: finisher-detection logic assumes exactly a 3-stage chain. Not a tuning knob.
    public static final int COMBO_LENGTH = 3;
    private static final double STAGGER_VISUAL_RADIUS = 24.0D;

    private static final Map<UUID, ComboState> STATES = new HashMap<>();

    private ComboCombat() {
    }

    public static PendingAttack prepareAttack(ServerPlayer player, Entity target, long gameTime,
                                               float attackStrength, float weaponDelayTicks) {
        ComboState state = STATES.computeIfAbsent(player.getUUID(), id -> new ComboState());
        int allowedGapTicks = Math.round(weaponDelayTicks) + ComboConfig.timeoutSlackTicks;

        int activeStage = state.stage;
        if (activeStage > 0 && gameTime - state.lastConfirmedTick > allowedGapTicks) {
            activeStage = 0;
        }

        if (attackStrength < ComboConfig.minAttackStrength) {
            return new PendingAttack(false, false, 1.0F, activeStage, gameTime, allowedGapTicks, false);
        }

        boolean backstab = isBackstab(player, target);
        float backstabFactor = backstab ? (float) ComboConfig.backstabDamageMultiplier : 1.0F;

        boolean parryArmed = state.parryFinisherExpiresTick > 0 && gameTime <= state.parryFinisherExpiresTick;
        if (parryArmed) {
            return new PendingAttack(true, true, (float) ComboConfig.finisherDamageMultiplier * backstabFactor,
                    COMBO_LENGTH, gameTime, allowedGapTicks, backstab);
        }

        int prospectiveStage = activeStage + 1;
        boolean finisher = prospectiveStage >= COMBO_LENGTH;
        float multiplier = (finisher ? (float) ComboConfig.finisherDamageMultiplier : 1.0F) * backstabFactor;
        return new PendingAttack(true, finisher, multiplier, prospectiveStage, gameTime, allowedGapTicks, backstab);
    }

    private static boolean isBackstab(ServerPlayer player, Entity target) {
        return player.isCrouching() && target instanceof LivingEntity && isBehind(player, target);
    }

    /**
     * Whether {@code mover} is standing within a configurable arc directly behind {@code facer}'s
     * own facing direction (both projected to the horizontal plane, so looking up or down doesn't
     * affect it). Shared by the backstab damage bonus and {@code StealthVisibilityMixin} (a mob's
     * own facing direction also determines how easily it notices a sneaking player approaching
     * from its blind spot).
     */
    public static boolean isBehind(Entity mover, Entity facer) {
        double dx = mover.getX() - facer.getX();
        double dz = mover.getZ() - facer.getZ();
        double lengthSq = dx * dx + dz * dz;
        if (lengthSq < 1.0E-4) {
            return false;
        }
        double length = Math.sqrt(lengthSq);
        double towardMoverX = dx / length;
        double towardMoverZ = dz / length;

        Vec3 facing = facer.getLookAngle();
        double facingLengthSq = facing.x * facing.x + facing.z * facing.z;
        if (facingLengthSq < 1.0E-4) {
            return false;
        }
        double facingLength = Math.sqrt(facingLengthSq);
        double facingX = facing.x / facingLength;
        double facingZ = facing.z / facingLength;

        double dot = towardMoverX * facingX + towardMoverZ * facingZ;
        double thresholdDot = Math.cos(Math.toRadians(180.0 - ComboConfig.backstabConeDegrees / 2.0));
        return dot <= thresholdDot;
    }

    public static ConfirmResult confirmAttack(ServerPlayer player, PendingAttack pending, boolean didDamage) {
        if (!didDamage || !pending.countsTowardCombo()) {
            return ConfirmResult.NONE;
        }

        ComboState state = STATES.computeIfAbsent(player.getUUID(), id -> new ComboState());
        state.lastConfirmedTick = pending.gameTime();

        if (pending.finisher()) {
            state.stage = 0;
            state.parryFinisherExpiresTick = 0;
            return new ConfirmResult(true, true, COMBO_LENGTH, 0);
        }

        state.stage = pending.prospectiveStage();
        return new ConfirmResult(true, false, state.stage, pending.allowedGapTicks());
    }

    /**
     * Arms a one-shot bonus: the next hit this player lands (on anything, before the window
     * expires) is treated as a finisher outright, bypassing the normal 3-stage progression.
     */
    public static void armParryFinisher(ServerPlayer player, long gameTime) {
        ComboState state = STATES.computeIfAbsent(player.getUUID(), id -> new ComboState());
        state.parryFinisherExpiresTick = gameTime + ComboConfig.parryFinisherWindowTicks;
    }

    public static void applyFinisherFeedback(ServerPlayer player, Entity target, ServerLevel level) {
        ComboStamina.drainSpike(player, (float) ComboConfig.staminaFinisherSpike);

        WeaponCategory category = WeaponCategory.resolve(player.getMainHandItem());
        double categoryMultiplier = switch (category) {
            case AXE -> ComboConfig.axeKnockbackMultiplier;
            case MACE -> ComboConfig.maceKnockbackMultiplier;
            default -> 1.0D;
        };
        double knockback = ComboConfig.finisherExtraKnockback * categoryMultiplier;

        double dx = player.getX() - target.getX();
        double dz = player.getZ() - target.getZ();
        double lengthSq = dx * dx + dz * dz;
        if (lengthSq > 1.0E-4) {
            double length = Math.sqrt(lengthSq);
            double nx = dx / length;
            double nz = dz / length;
            if (target instanceof LivingEntity livingTarget) {
                // Direction convention matches vanilla LivingEntity#actuallyHurt: push away from the attacker.
                //? if >=26.2 {
                /*livingTarget.knockback(knockback, nx, nz, player.damageSources().playerAttack(player), 0.0F, true);
                *///? } else {
                livingTarget.knockback(knockback, nx, nz);
                //? }
            } else {
                target.push(-nx * knockback, 0.1D, -nz * knockback);
            }
        }

        level.playSound(null, target.getX(), target.getY(), target.getZ(),
                category.finisherSound(), SoundSource.PLAYERS, 1.0F, 1.0F);
        level.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ(),
                14, 0.3D, 0.35D, 0.3D, 0.05D);

        if (target instanceof LivingEntity livingTarget) {
            applyStagger(livingTarget, level);

            if (category == WeaponCategory.SWORD && ComboConfig.swordBleedDurationTicks > 0) {
                livingTarget.addEffect(new MobEffectInstance(
                        MobEffects.WITHER, ComboConfig.swordBleedDurationTicks, ComboConfig.swordBleedAmplifier));
            }
        }

        if (category == WeaponCategory.MACE) {
            applyMaceSplash(player, target, level);
        }
    }

    public static void applyBackstabFeedback(ServerPlayer player, Entity target, ServerLevel level) {
        level.playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0F, 1.6F);

        // Display preference is read client-side (see Combo.java's receiver) rather than gated
        // here, since the server's own config copy isn't necessarily the attacking player's.
        NetworkManager.sendToPlayer(player, new ComboBackstabPayload());
    }

    /** Reused by both the finisher payoff and a successful parry. */
    public static void applyStagger(LivingEntity target, ServerLevel level) {
        int duration = ComboConfig.staggerDurationTicks;
        if (duration <= 0) {
            return;
        }

        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, duration, ComboConfig.staggerSlownessAmplifier));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, ComboConfig.staggerWeaknessAmplifier));

        NetworkManager.sendToPlayers(
                level.getPlayers(p -> p.distanceToSqr(target) <= STAGGER_VISUAL_RADIUS * STAGGER_VISUAL_RADIUS),
                new ComboStaggerPayload(target.getId(), duration));
    }

    private static void applyMaceSplash(ServerPlayer player, Entity target, ServerLevel level) {
        double radius = ComboConfig.maceAoeRadius;
        if (radius <= 0.0D) {
            return;
        }

        float splashDamage = (float) ComboConfig.maceAoeDamage;
        DamageSource source = player.damageSources().playerAttack(player);
        AABB area = target.getBoundingBox().inflate(radius);

        for (LivingEntity nearby : level.getEntities(EntityTypeTest.forClass(LivingEntity.class), area,
                e -> e != target && e != player && e.isAlive())) {
            float healthBefore = nearby.getHealth();
            nearby.hurt(source, splashDamage);

            float dealt = healthBefore - nearby.getHealth();
            if (dealt > 0.0F) {
                NetworkManager.sendToPlayer(player, new ComboDamagePopupPayload(nearby.getId(), dealt, false));
            }
        }
    }

    public static void clear(Player player) {
        STATES.remove(player.getUUID());
    }

    public record PendingAttack(boolean countsTowardCombo, boolean finisher, float damageMultiplier,
                                 int prospectiveStage, long gameTime, int allowedGapTicks, boolean backstab) {
    }

    public record ConfirmResult(boolean advanced, boolean finisher, int stage, int remainingTicks) {
        static final ConfirmResult NONE = new ConfirmResult(false, false, 0, 0);
    }
}
