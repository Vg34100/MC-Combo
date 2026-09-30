package net.vg.combo.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.vg.combo.combat.ComboCombat;
import net.vg.combo.combat.ComboStamina;
import net.vg.combo.config.ComboConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * {@code LivingEntity#getVisibilityPercent(Entity looker)} is what {@code TargetingConditions#test}
 * (used by mob target-acquisition goals, confirmed via bytecode) multiplies detection range by.
 * Vanilla's own sneaking reduction there is only -20% ({@code isDiscrete()} multiplies by 0.8) —
 * nowhere near enough to let a sneaking player approach a mob without being noticed from typical
 * engage distances, even from directly behind it. This is invoked as
 * {@code target.getVisibilityPercent(looker)}, so {@code this} here is the entity being looked
 * *at* (the potentially-sneaking player) and the sole parameter is the entity doing the looking
 * (the mob).
 *
 * Full Sneak Mode is {@code ComboStamina.isFullSneakActive} — real crouching, config enabled, and
 * stamina remaining, no separate toggle to remember to turn on/off. Standing up (or the config
 * being off, or running out of stamina) always and immediately reverts to plain vanilla detection,
 * no desync possible. Within a target's blind spot (see {@code ComboCombat#isBehind}, the same
 * geometry used for the backstab damage bonus) it's full immunity; everywhere else it's still
 * drastically reduced, just not to zero.
 */
@Mixin(LivingEntity.class)
public abstract class StealthVisibilityMixin {
    @Inject(method = "getVisibilityPercent", at = @At("RETURN"), cancellable = true)
    private void combo$reduceVisibilityWhenSneaking(Entity looker, CallbackInfoReturnable<Double> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof Player player) || looker == null || !self.isDiscrete()) {
            return;
        }

        boolean behind = ComboCombat.isBehind(self, looker);

        if (ComboStamina.isFullSneakActive(player)) {
            cir.setReturnValue(behind ? 0.0D : cir.getReturnValue() * ComboConfig.fullSneakOmniVisibilityMultiplier);
        } else if (behind) {
            cir.setReturnValue(cir.getReturnValue() * ComboConfig.sneakVisibilityMultiplier);
        }
    }
}
