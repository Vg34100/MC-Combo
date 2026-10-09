package net.vg.combo.client;

import com.mojang.serialization.Codec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;
import net.vg.combo.config.ComboConfig;

import java.util.function.Consumer;

/**
 * Standard vanilla options screen (same base class Minecraft's own settings screens use), so it
 * gets the usual label+value slider rows and a "Done" button for free, and behaves consistently
 * whether opened from ModMenu or NeoForge's native config screen entry point.
 */
public final class ComboConfigScreen extends OptionsSubScreen {
    private static final Component TITLE = Component.translatable("combo.config.title");

    public ComboConfigScreen(Screen parent) {
        super(parent, Minecraft.getInstance().options, TITLE);
    }

    @Override
    protected void addOptions() {
        OptionInstance<Double> minAttackStrength = doubleOption(
                "combo.min_attack_strength", 0.0D, 1.0D, 100,
                ComboConfig.minAttackStrength, value -> ComboConfig.minAttackStrength = value,
                value -> String.format("%.0f%%", value * 100.0));

        OptionInstance<Integer> timeoutSlackTicks = intOption(
                "combo.timeout_slack_ticks", 0, 40,
                ComboConfig.timeoutSlackTicks, value -> ComboConfig.timeoutSlackTicks = value);

        OptionInstance<Double> finisherDamageMultiplier = doubleOption(
                "combo.finisher_damage_multiplier", 1.0D, 5.0D, 80,
                ComboConfig.finisherDamageMultiplier, value -> ComboConfig.finisherDamageMultiplier = value,
                value -> String.format("%.2fx", value));

        OptionInstance<Double> finisherExtraKnockback = doubleOption(
                "combo.finisher_extra_knockback", 0.0D, 3.0D, 60,
                ComboConfig.finisherExtraKnockback, value -> ComboConfig.finisherExtraKnockback = value,
                value -> String.format("%.2f", value));

        OptionInstance<Double> axeKnockbackMultiplier = doubleOption(
                "combo.axe_knockback_multiplier", 1.0D, 3.0D, 40,
                ComboConfig.axeKnockbackMultiplier, value -> ComboConfig.axeKnockbackMultiplier = value,
                value -> String.format("%.2fx", value));

        OptionInstance<Double> maceKnockbackMultiplier = doubleOption(
                "combo.mace_knockback_multiplier", 1.0D, 4.0D, 60,
                ComboConfig.maceKnockbackMultiplier, value -> ComboConfig.maceKnockbackMultiplier = value,
                value -> String.format("%.2fx", value));

        OptionInstance<Boolean> damagePopupEnabled = booleanOption(
                "combo.damage_popup_enabled", ComboConfig.damagePopupEnabled, value -> ComboConfig.damagePopupEnabled = value);

        OptionInstance<Integer> staggerDurationTicks = intOption(
                "combo.stagger_duration_ticks", 0, 100,
                ComboConfig.staggerDurationTicks, value -> ComboConfig.staggerDurationTicks = value);

        OptionInstance<Integer> staggerSlownessAmplifier = intOption(
                "combo.stagger_slowness_amplifier", 0, 5,
                ComboConfig.staggerSlownessAmplifier, value -> ComboConfig.staggerSlownessAmplifier = value);

        OptionInstance<Integer> staggerWeaknessAmplifier = intOption(
                "combo.stagger_weakness_amplifier", 0, 5,
                ComboConfig.staggerWeaknessAmplifier, value -> ComboConfig.staggerWeaknessAmplifier = value);

        OptionInstance<Boolean> staggerVisualEnabled = booleanOption(
                "combo.stagger_visual_enabled", ComboConfig.staggerVisualEnabled, value -> ComboConfig.staggerVisualEnabled = value);

        OptionInstance<Integer> swordBleedDurationTicks = intOption(
                "combo.sword_bleed_duration_ticks", 0, 120,
                ComboConfig.swordBleedDurationTicks, value -> ComboConfig.swordBleedDurationTicks = value);

        OptionInstance<Integer> swordBleedAmplifier = intOption(
                "combo.sword_bleed_amplifier", 0, 5,
                ComboConfig.swordBleedAmplifier, value -> ComboConfig.swordBleedAmplifier = value);

        OptionInstance<Double> maceAoeRadius = doubleOption(
                "combo.mace_aoe_radius", 0.0D, 8.0D, 32,
                ComboConfig.maceAoeRadius, value -> ComboConfig.maceAoeRadius = value,
                value -> String.format("%.1f", value));

        OptionInstance<Double> maceAoeDamage = doubleOption(
                "combo.mace_aoe_damage", 0.0D, 20.0D, 40,
                ComboConfig.maceAoeDamage, value -> ComboConfig.maceAoeDamage = value,
                value -> String.format("%.1f", value));

        OptionInstance<Integer> parryWindowTicks = intOption(
                "combo.parry_window_ticks", 0, 30,
                ComboConfig.parryWindowTicks, value -> ComboConfig.parryWindowTicks = value);

        OptionInstance<Double> parryKnockback = doubleOption(
                "combo.parry_knockback", 0.0D, 3.0D, 60,
                ComboConfig.parryKnockback, value -> ComboConfig.parryKnockback = value,
                value -> String.format("%.2f", value));

        OptionInstance<Integer> parryFinisherWindowTicks = intOption(
                "combo.parry_finisher_window_ticks", 0, 100,
                ComboConfig.parryFinisherWindowTicks, value -> ComboConfig.parryFinisherWindowTicks = value);

        OptionInstance<Double> shieldBlockDelayMultiplier = doubleOption(
                "combo.shield_block_delay_multiplier", 0.0D, 1.0D, 100,
                ComboConfig.shieldBlockDelayMultiplier, value -> ComboConfig.shieldBlockDelayMultiplier = value,
                value -> String.format("%.0f%%", value * 100.0));

        OptionInstance<Double> backstabDamageMultiplier = doubleOption(
                "combo.backstab_damage_multiplier", 1.0D, 3.0D, 40,
                ComboConfig.backstabDamageMultiplier, value -> ComboConfig.backstabDamageMultiplier = value,
                value -> String.format("%.2fx", value));

        OptionInstance<Integer> backstabConeDegrees = intOption(
                "combo.backstab_cone_degrees", 30, 180,
                ComboConfig.backstabConeDegrees, value -> ComboConfig.backstabConeDegrees = value);

        OptionInstance<Boolean> backstabVignetteEnabled = booleanOption(
                "combo.backstab_vignette_enabled", ComboConfig.backstabVignetteEnabled,
                value -> ComboConfig.backstabVignetteEnabled = value);

        OptionInstance<Double> sneakVisibilityMultiplier = doubleOption(
                "combo.sneak_visibility_multiplier", 0.0D, 1.0D, 100,
                ComboConfig.sneakVisibilityMultiplier, value -> ComboConfig.sneakVisibilityMultiplier = value,
                value -> String.format("%.0f%%", value * 100.0));

        OptionInstance<Double> takedownRange = doubleOption(
                "combo.takedown_range", 0.5D, 4.0D, 35,
                ComboConfig.takedownRange, value -> ComboConfig.takedownRange = value,
                value -> String.format("%.1f", value));

        OptionInstance<Double> takedownDamageMultiplier = doubleOption(
                "combo.takedown_damage_multiplier", 1.0D, 15.0D, 70,
                ComboConfig.takedownDamageMultiplier, value -> ComboConfig.takedownDamageMultiplier = value,
                value -> String.format("%.1fx", value));

        OptionInstance<Boolean> fullSneakModeEnabled = booleanOption(
                "combo.full_sneak_mode_enabled", ComboConfig.fullSneakModeEnabled,
                value -> ComboConfig.fullSneakModeEnabled = value);

        OptionInstance<Double> fullSneakOmniVisibilityMultiplier = doubleOption(
                "combo.full_sneak_omni_visibility_multiplier", 0.0D, 1.0D, 100,
                ComboConfig.fullSneakOmniVisibilityMultiplier, value -> ComboConfig.fullSneakOmniVisibilityMultiplier = value,
                value -> String.format("%.0f%%", value * 100.0));

        OptionInstance<Double> staminaMax = doubleOption(
                "combo.stamina_max", 20.0D, 200.0D, 90,
                ComboConfig.staminaMax, value -> ComboConfig.staminaMax = value,
                value -> String.format("%.0f", value));

        OptionInstance<Double> staminaRegenPerTick = doubleOption(
                "combo.stamina_regen_per_tick", 0.1D, 3.0D, 58,
                ComboConfig.staminaRegenPerTick, value -> ComboConfig.staminaRegenPerTick = value,
                value -> String.format("%.2f", value));

        OptionInstance<Integer> staminaRegenDelayTicks = intOption(
                "combo.stamina_regen_delay_ticks", 0, 100,
                ComboConfig.staminaRegenDelayTicks, value -> ComboConfig.staminaRegenDelayTicks = value);

        OptionInstance<Double> staminaSprintDrainPerTick = doubleOption(
                "combo.stamina_sprint_drain_per_tick", 0.0D, 5.0D, 100,
                ComboConfig.staminaSprintDrainPerTick, value -> ComboConfig.staminaSprintDrainPerTick = value,
                value -> String.format("%.2f", value));

        OptionInstance<Double> staminaBlockDrainPerTick = doubleOption(
                "combo.stamina_block_drain_per_tick", 0.0D, 5.0D, 100,
                ComboConfig.staminaBlockDrainPerTick, value -> ComboConfig.staminaBlockDrainPerTick = value,
                value -> String.format("%.2f", value));

        OptionInstance<Double> staminaFullSneakDrainPerTick = doubleOption(
                "combo.stamina_full_sneak_drain_per_tick", 0.0D, 5.0D, 100,
                ComboConfig.staminaFullSneakDrainPerTick, value -> ComboConfig.staminaFullSneakDrainPerTick = value,
                value -> String.format("%.2f", value));

        OptionInstance<Double> staminaDodgeCost = doubleOption(
                "combo.stamina_dodge_cost", 0.0D, 100.0D, 100,
                ComboConfig.staminaDodgeCost, value -> ComboConfig.staminaDodgeCost = value,
                value -> String.format("%.0f", value));

        OptionInstance<Double> staminaParrySpike = doubleOption(
                "combo.stamina_parry_spike", 0.0D, 50.0D, 100,
                ComboConfig.staminaParrySpike, value -> ComboConfig.staminaParrySpike = value,
                value -> String.format("%.0f", value));

        OptionInstance<Double> staminaFinisherSpike = doubleOption(
                "combo.stamina_finisher_spike", 0.0D, 50.0D, 100,
                ComboConfig.staminaFinisherSpike, value -> ComboConfig.staminaFinisherSpike = value,
                value -> String.format("%.0f", value));

        OptionInstance<Double> staminaTakedownSpike = doubleOption(
                "combo.stamina_takedown_spike", 0.0D, 50.0D, 100,
                ComboConfig.staminaTakedownSpike, value -> ComboConfig.staminaTakedownSpike = value,
                value -> String.format("%.0f", value));

        OptionInstance<Double> dodgeStrength = doubleOption(
                "combo.dodge_strength", 0.1D, 2.5D, 48,
                ComboConfig.dodgeStrength, value -> ComboConfig.dodgeStrength = value,
                value -> String.format("%.2f", value));

        OptionInstance<Integer> dodgeInvulnerabilityTicks = intOption(
                "combo.dodge_invulnerability_ticks", 0, 30,
                ComboConfig.dodgeInvulnerabilityTicks, value -> ComboConfig.dodgeInvulnerabilityTicks = value);

        OptionInstance<Integer> dodgeCooldownTicks = intOption(
                "combo.dodge_cooldown_ticks", 0, 60,
                ComboConfig.dodgeCooldownTicks, value -> ComboConfig.dodgeCooldownTicks = value);

        OptionInstance<?>[] options = new OptionInstance[]{
                minAttackStrength,
                timeoutSlackTicks,
                finisherDamageMultiplier,
                finisherExtraKnockback,
                axeKnockbackMultiplier,
                maceKnockbackMultiplier,
                damagePopupEnabled,
                staggerDurationTicks,
                staggerSlownessAmplifier,
                staggerWeaknessAmplifier,
                staggerVisualEnabled,
                swordBleedDurationTicks,
                swordBleedAmplifier,
                maceAoeRadius,
                maceAoeDamage,
                parryWindowTicks,
                parryKnockback,
                parryFinisherWindowTicks,
                shieldBlockDelayMultiplier,
                backstabDamageMultiplier,
                backstabConeDegrees,
                backstabVignetteEnabled,
                sneakVisibilityMultiplier,
                takedownRange,
                takedownDamageMultiplier,
                fullSneakModeEnabled,
                fullSneakOmniVisibilityMultiplier,
                staminaMax,
                staminaRegenPerTick,
                staminaRegenDelayTicks,
                staminaSprintDrainPerTick,
                staminaBlockDrainPerTick,
                staminaFullSneakDrainPerTick,
                staminaDodgeCost,
                staminaParrySpike,
                staminaFinisherSpike,
                staminaTakedownSpike,
                dodgeStrength,
                dodgeInvulnerabilityTicks,
                dodgeCooldownTicks
        };
        this.list.addSmall(options);
    }

    @Override
    public void onClose() {
        ComboConfig.saveConfigs();
        //? if >=26.2 {
        /*this.minecraft.gui.setScreen(this.lastScreen);
        *///? } else {
        this.minecraft.setScreen(this.lastScreen);
        //? }
    }

    private static OptionInstance<Double> doubleOption(String key, double min, double max, int steps,
                                                         double current, Consumer<Double> setter,
                                                         java.util.function.Function<Double, String> format) {
        return new OptionInstance<>(
                key,
                OptionInstance.cachedConstantTooltip(Component.translatable("tooltip." + key)),
                (optionText, value) -> genericValue(optionText, format.apply(value)),
                new OptionInstance.IntRange(0, steps).xmap(
                        sliderValue -> min + (max - min) * sliderValue / steps,
                        value -> (int) Math.round((value - min) / (max - min) * steps)
                        //? if >=26.1 {
                        , true
                        //? }
                ),
                Codec.doubleRange(min, max),
                current,
                setter::accept
        );
    }

    private static OptionInstance<Integer> intOption(String key, int min, int max, int current, Consumer<Integer> setter) {
        return new OptionInstance<>(
                key,
                OptionInstance.cachedConstantTooltip(Component.translatable("tooltip." + key)),
                (optionText, value) -> genericValue(optionText, String.valueOf(value)),
                new OptionInstance.IntRange(min, max),
                current,
                setter::accept
        );
    }

    private static OptionInstance<Boolean> booleanOption(String key, boolean current, Consumer<Boolean> setter) {
        return OptionInstance.createBoolean(
                key,
                OptionInstance.cachedConstantTooltip(Component.translatable("tooltip." + key)),
                current,
                setter::accept
        );
    }

    private static Component genericValue(Component prefix, String value) {
        return Component.translatable("options.generic_value", prefix, Component.literal(value));
    }
}
