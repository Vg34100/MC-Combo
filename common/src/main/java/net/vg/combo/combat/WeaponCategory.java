package net.vg.combo.combat;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;

/**
 * Broad weapon grouping used to vary finisher feedback. Resolved from vanilla's own
 * {@link ItemTags#SWORDS}/{@link ItemTags#AXES} rather than {@code instanceof} checks on
 * concrete item classes, so modded weapons that use those tags are still categorized correctly.
 * Mace is the exception: vanilla has no purpose-built tag for it (only one vanilla item exists
 * in that category), so it's resolved via {@code instanceof MaceItem} instead.
 */
public enum WeaponCategory {
    SWORD(SoundEvents.PLAYER_ATTACK_CRIT),
    AXE(SoundEvents.PLAYER_ATTACK_KNOCKBACK),
    MACE(SoundEvents.MACE_SMASH_GROUND_HEAVY),
    OTHER(SoundEvents.PLAYER_ATTACK_CRIT);

    private final SoundEvent finisherSound;

    WeaponCategory(SoundEvent finisherSound) {
        this.finisherSound = finisherSound;
    }

    public static WeaponCategory resolve(ItemStack weapon) {
        if (weapon.getItem() instanceof MaceItem) {
            return MACE;
        }
        if (weapon.is(ItemTags.AXES)) {
            return AXE;
        }
        if (weapon.is(ItemTags.SWORDS)) {
            return SWORD;
        }
        return OTHER;
    }

    public SoundEvent finisherSound() {
        return finisherSound;
    }
}
