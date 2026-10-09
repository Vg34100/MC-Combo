package net.vg.combo.mixin;

import com.mojang.logging.LogUtils;
import dev.architectury.networking.NetworkManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
//? if >=26.1 {
import net.minecraft.world.entity.animal.golem.IronGolem;
//? } else {
/*import net.minecraft.world.entity.animal.IronGolem;
*///? }
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Monster;
//? if >=26.1 {
import net.minecraft.world.entity.npc.villager.AbstractVillager;
//? } else {
/*import net.minecraft.world.entity.npc.AbstractVillager;
*///? }
import net.minecraft.world.entity.player.Player;
import net.vg.combo.combat.ComboCombat;
import net.vg.combo.combat.ComboStamina;
import net.vg.combo.config.ComboConfig;
import net.vg.combo.network.ComboDamagePopupPayload;
import net.vg.combo.network.ComboSyncPayload;
import net.vg.combo.stats.ComboStats;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Player.class)
public abstract class PlayerAttackMixin {
    private static final Logger COMBO_LOGGER = LogUtils.getLogger();

    // Vanilla reads this early in attack() and stores it locally before onAttack() resets the
    // strength ticker for the next swing. hurtOrSimulate runs after that reset, so the redirect
    // below must reuse this captured value instead of re-reading a value already zeroed.
    @Unique
    private float combo$capturedAttackStrength;

    @Redirect(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;getAttackStrengthScale(F)F"
            )
    )
    private float combo$captureAttackStrength(Player instance, float adjustTicks) {
        float scale = instance.getAttackStrengthScale(adjustTicks);
        this.combo$capturedAttackStrength = scale;
        return scale;
    }

    @Redirect(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    //? if >=26.1 {
                    target = "Lnet/minecraft/world/entity/Entity;hurtOrSimulate(Lnet/minecraft/world/damagesource/DamageSource;F)Z"
                    //? } else {
                    /*target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"
                    *///? }
            )
    )
    private boolean combo$redirectAttackDamage(Entity target, DamageSource source, float amount) {
        Player self = (Player) (Object) this;
        if (!(self instanceof ServerPlayer serverPlayer) || !(self.level() instanceof ServerLevel serverLevel)) {
            //? if >=26.1 {
            return target.hurtOrSimulate(source, amount);
            //? } else {
            /*return target.hurt(source, amount);
            *///? }
        }

        boolean takedown = combo$isTakedownEligible(serverPlayer, target);

        float strength = this.combo$capturedAttackStrength;
        float weaponDelayTicks = serverPlayer.getCurrentItemAttackStrengthDelay();
        ComboCombat.PendingAttack pending = ComboCombat.prepareAttack(
                serverPlayer, target, serverLevel.getGameTime(), strength, weaponDelayTicks);

        float totalMultiplier = pending.damageMultiplier() * (takedown ? (float) ComboConfig.takedownDamageMultiplier : 1.0F);

        float healthBefore = target instanceof LivingEntity livingTarget ? livingTarget.getHealth() : 0.0F;
        //? if >=26.1 {
        boolean damaged = target.hurtOrSimulate(source, amount * totalMultiplier);
        //? } else {
        /*boolean damaged = target.hurt(source, amount * totalMultiplier);
        *///? }
        ComboCombat.ConfirmResult result = ComboCombat.confirmAttack(serverPlayer, pending, damaged);

        COMBO_LOGGER.debug(
                "combo: target={} strength={} weaponDelay={} counts={} damaged={} advanced={} stage={} finisher={} takedown={}",
                target.getName().getString(), strength, weaponDelayTicks, pending.countsTowardCombo(), damaged,
                result.advanced(), result.stage(), result.finisher(), takedown);

        if (damaged && target instanceof LivingEntity livingTarget) {
            // Health delta accounts for armor/enchantment/absorption reduction, unlike the
            // pre-mitigation `amount` float above.
            float dealt = healthBefore - livingTarget.getHealth();
            if (dealt > 0.0F) {
                serverPlayer.awardStat(ComboStats.DAMAGE_DEALT.get(), Math.round(dealt));
                NetworkManager.sendToPlayer(serverPlayer,
                        new ComboDamagePopupPayload(target.getId(), dealt, result.finisher()));
            }

            if (pending.backstab()) {
                ComboCombat.applyBackstabFeedback(serverPlayer, target, serverLevel);
            }

            if (takedown) {
                serverLevel.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                        SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0F, 0.7F);
                ComboStamina.drainSpike(serverPlayer, (float) ComboConfig.staminaTakedownSpike);
            }
        }

        if (result.advanced()) {
            serverPlayer.awardStat(ComboStats.HITS_LANDED.get(), 1);
            NetworkManager.sendToPlayer(serverPlayer, new ComboSyncPayload(result.stage(), result.remainingTicks(), result.finisher()));
            if (result.finisher()) {
                serverPlayer.awardStat(ComboStats.FINISHERS_LANDED.get(), 1);
                ComboCombat.applyFinisherFeedback(serverPlayer, target, serverLevel);
            }
        }

        return damaged;
    }

    /**
     * Full Sneak Mode (real crouching + {@code ComboConfig.fullSneakModeEnabled}, see
     * {@code StealthVisibilityMixin}) + directly behind a target + close enough is a heavily
     * bonus-damage hit ({@code ComboConfig.takedownDamageMultiplier}), routed through the exact
     * same {@code hurtOrSimulate} call as any other attack rather than a raw {@code kill()} — that
     * matters, since a raw kill bypassed things like a Creaking's damage immunity outright. Fodder
     * hostiles still die in one hit; tanky ones (Iron Golem, Warden) take a serious hit but live.
     *
     * Eligible targets are hostile mobs, Iron Golems, and villagers — not true bosses, and not
     * anything already targeting the attacking player (a takedown is meant for something that
     * hasn't noticed you yet, not a shortcut mid-fight).
     */
    @Unique
    private boolean combo$isTakedownEligible(ServerPlayer serverPlayer, Entity target) {
        if (!ComboStamina.isFullSneakActive(serverPlayer)) {
            return false;
        }
        if (!(target instanceof Mob mob) || !mob.isAlive()
                || target instanceof WitherBoss || target instanceof EnderDragon) {
            return false;
        }
        if (!(mob instanceof Monster || mob instanceof IronGolem || mob instanceof AbstractVillager)) {
            return false;
        }
        if (mob.getTarget() == serverPlayer) {
            return false;
        }
        return ComboCombat.isBehind(serverPlayer, target) && serverPlayer.distanceTo(target) <= ComboConfig.takedownRange;
    }
}
