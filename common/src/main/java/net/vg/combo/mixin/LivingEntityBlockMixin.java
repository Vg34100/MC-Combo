package net.vg.combo.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.vg.combo.combat.ComboParry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * {@code blockedByItem(LivingEntity)} is invoked on the ATTACKER (not the defender) from inside
 * {@code blockUsingItem} — {@code attacker.blockedByItem(defender)} — confirmed via 26.1.2
 * bytecode: {@code blockUsingItem} pushes the attacker parameter as the invokevirtual receiver and
 * {@code this} (the defender) as the argument, and {@code blockedByItem}'s own body knocks the
 * argument back away from {@code this}, matching vanilla's small "recoil on the blocker" shield
 * bump. Only fires from {@code applyItemBlocking} when the block actually reduced damage and the
 * source wasn't a projectile, so this only ever sees a genuinely successful non-projectile block.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityBlockMixin {
    @Inject(method = "blockedByItem", at = @At("HEAD"))
    private void combo$onBlockedItem(LivingEntity defender, CallbackInfo ci) {
        LivingEntity attacker = (LivingEntity) (Object) this;
        if (defender instanceof ServerPlayer player && attacker.level() instanceof ServerLevel serverLevel) {
            ComboParry.onBlockedHit(player, attacker, serverLevel);
        }
    }
}
