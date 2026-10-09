package net.vg.combo.mixin;

//? if >=26.1 {
import net.minecraft.world.item.component.BlocksAttacks;
//? } else {
/*import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
*///? }
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
//? if >=26.1 {
@Mixin(BlocksAttacks.class)
//? } else {
/*@Mixin(LivingEntity.class)
*///? }
public abstract class BlocksAttacksMixin {
    //? if >=26.1 {
    @Inject(method = "blockDelayTicks", at = @At("RETURN"), cancellable = true)
    private void combo$scaleBlockDelay(CallbackInfoReturnable<Integer> cir) {
        int scaled = Math.max(0, Math.round(cir.getReturnValue() * (float) ComboConfig.shieldBlockDelayMultiplier));
        cir.setReturnValue(scaled);
    }
    //? } else {
    /*// Legacy shields keep their five-tick windup in LivingEntity.isBlocking().
    @ModifyConstant(method = "isBlocking", constant = @Constant(intValue = 5))
    private int combo$scaleBlockDelay(int vanillaDelay) {
        return Math.max(0, Math.round(vanillaDelay * (float) ComboConfig.shieldBlockDelayMultiplier));
    }
    *///? }
}
