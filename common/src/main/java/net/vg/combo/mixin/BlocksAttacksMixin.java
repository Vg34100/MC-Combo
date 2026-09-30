package net.vg.combo.mixin;

import net.minecraft.world.item.component.BlocksAttacks;
import net.vg.combo.config.ComboConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * {@code blockDelayTicks() = Math.round(blockDelaySeconds * 20.0F)} — confirmed via 26.1.2
 * bytecode. Vanilla's shield uses {@code blockDelaySeconds = 0.25} (5 ticks), a real windup after
 * raising the shield before {@code LivingEntity#getItemBlockingWith()} treats it as actually
 * blocking (see {@code LivingEntity#getItemBlockingWith}, which gates on
 * {@code ticksUsed >= blockDelayTicks()}). Scaling this down gives a reactively-raised shield a
 * fairer chance of being "up" in time for a well-timed block/parry.
 */
@Mixin(BlocksAttacks.class)
public abstract class BlocksAttacksMixin {
    @Inject(method = "blockDelayTicks", at = @At("RETURN"), cancellable = true)
    private void combo$scaleBlockDelay(CallbackInfoReturnable<Integer> cir) {
        int scaled = Math.max(0, Math.round(cir.getReturnValue() * (float) ComboConfig.shieldBlockDelayMultiplier));
        cir.setReturnValue(scaled);
    }
}
