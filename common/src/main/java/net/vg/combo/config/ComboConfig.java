package net.vg.combo.config;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;

public final class ComboConfig {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String KEY_MIN_ATTACK_STRENGTH = "combo.min_attack_strength";
    private static final String KEY_TIMEOUT_SLACK_TICKS = "combo.timeout_slack_ticks";
    private static final String KEY_FINISHER_DAMAGE_MULTIPLIER = "combo.finisher_damage_multiplier";
    private static final String KEY_FINISHER_EXTRA_KNOCKBACK = "combo.finisher_extra_knockback";
    private static final String KEY_AXE_KNOCKBACK_MULTIPLIER = "combo.axe_knockback_multiplier";
    private static final String KEY_MACE_KNOCKBACK_MULTIPLIER = "combo.mace_knockback_multiplier";
    private static final String KEY_DAMAGE_POPUP_ENABLED = "combo.damage_popup_enabled";
    private static final String KEY_STAGGER_DURATION_TICKS = "combo.stagger_duration_ticks";
    private static final String KEY_STAGGER_SLOWNESS_AMPLIFIER = "combo.stagger_slowness_amplifier";
    private static final String KEY_STAGGER_WEAKNESS_AMPLIFIER = "combo.stagger_weakness_amplifier";
    private static final String KEY_STAGGER_VISUAL_ENABLED = "combo.stagger_visual_enabled";
    private static final String KEY_SWORD_BLEED_DURATION_TICKS = "combo.sword_bleed_duration_ticks";
    private static final String KEY_SWORD_BLEED_AMPLIFIER = "combo.sword_bleed_amplifier";
    private static final String KEY_MACE_AOE_RADIUS = "combo.mace_aoe_radius";
    private static final String KEY_MACE_AOE_DAMAGE = "combo.mace_aoe_damage";
    private static final String KEY_PARRY_WINDOW_TICKS = "combo.parry_window_ticks";
    private static final String KEY_PARRY_KNOCKBACK = "combo.parry_knockback";
    private static final String KEY_PARRY_FINISHER_WINDOW_TICKS = "combo.parry_finisher_window_ticks";
    private static final String KEY_SHIELD_BLOCK_DELAY_MULTIPLIER = "combo.shield_block_delay_multiplier";
    private static final String KEY_BACKSTAB_DAMAGE_MULTIPLIER = "combo.backstab_damage_multiplier";
    private static final String KEY_BACKSTAB_CONE_DEGREES = "combo.backstab_cone_degrees";
    private static final String KEY_BACKSTAB_VIGNETTE_ENABLED = "combo.backstab_vignette_enabled";
    private static final String KEY_SNEAK_VISIBILITY_MULTIPLIER = "combo.sneak_visibility_multiplier";
    private static final String KEY_TAKEDOWN_RANGE = "combo.takedown_range";
    private static final String KEY_TAKEDOWN_DAMAGE_MULTIPLIER = "combo.takedown_damage_multiplier";
    private static final String KEY_FULL_SNEAK_MODE_ENABLED = "combo.full_sneak_mode_enabled";
    private static final String KEY_FULL_SNEAK_OMNI_VISIBILITY_MULTIPLIER = "combo.full_sneak_omni_visibility_multiplier";
    private static final String KEY_STAMINA_MAX = "combo.stamina_max";
    private static final String KEY_STAMINA_REGEN_PER_TICK = "combo.stamina_regen_per_tick";
    private static final String KEY_STAMINA_REGEN_DELAY_TICKS = "combo.stamina_regen_delay_ticks";
    private static final String KEY_STAMINA_SPRINT_DRAIN_PER_TICK = "combo.stamina_sprint_drain_per_tick";
    private static final String KEY_STAMINA_BLOCK_DRAIN_PER_TICK = "combo.stamina_block_drain_per_tick";
    private static final String KEY_STAMINA_FULL_SNEAK_DRAIN_PER_TICK = "combo.stamina_full_sneak_drain_per_tick";
    private static final String KEY_STAMINA_DODGE_COST = "combo.stamina_dodge_cost";
    private static final String KEY_STAMINA_PARRY_SPIKE = "combo.stamina_parry_spike";
    private static final String KEY_STAMINA_FINISHER_SPIKE = "combo.stamina_finisher_spike";
    private static final String KEY_STAMINA_TAKEDOWN_SPIKE = "combo.stamina_takedown_spike";
    private static final String KEY_DODGE_STRENGTH = "combo.dodge_strength";
    private static final String KEY_DODGE_INVULNERABILITY_TICKS = "combo.dodge_invulnerability_ticks";
    private static final String KEY_DODGE_COOLDOWN_TICKS = "combo.dodge_cooldown_ticks";

    public static final double DEFAULT_MIN_ATTACK_STRENGTH = 0.90D;
    public static final int DEFAULT_TIMEOUT_SLACK_TICKS = 6;
    public static final double DEFAULT_FINISHER_DAMAGE_MULTIPLIER = 1.75D;
    public static final double DEFAULT_FINISHER_EXTRA_KNOCKBACK = 0.65D;
    public static final double DEFAULT_AXE_KNOCKBACK_MULTIPLIER = 1.5D;
    public static final double DEFAULT_MACE_KNOCKBACK_MULTIPLIER = 2.0D;
    public static final boolean DEFAULT_DAMAGE_POPUP_ENABLED = true;
    public static final int DEFAULT_STAGGER_DURATION_TICKS = 40;
    public static final int DEFAULT_STAGGER_SLOWNESS_AMPLIFIER = 2;
    public static final int DEFAULT_STAGGER_WEAKNESS_AMPLIFIER = 1;
    public static final boolean DEFAULT_STAGGER_VISUAL_ENABLED = true;
    public static final int DEFAULT_SWORD_BLEED_DURATION_TICKS = 60;
    public static final int DEFAULT_SWORD_BLEED_AMPLIFIER = 0;
    public static final double DEFAULT_MACE_AOE_RADIUS = 3.0D;
    public static final double DEFAULT_MACE_AOE_DAMAGE = 4.0D;
    // Deliberately tight — a parry should reward a just-in-time block, not "was already blocking".
    public static final int DEFAULT_PARRY_WINDOW_TICKS = 4;
    public static final double DEFAULT_PARRY_KNOCKBACK = 0.3D;
    public static final int DEFAULT_PARRY_FINISHER_WINDOW_TICKS = 40;
    // Vanilla's own shield has a real windup (BlocksAttacks#blockDelayTicks, 5 ticks by default)
    // before an actively-raised shield starts blocking anything at all, independent of this mod's
    // own parry-timing check — scaled down so a reactive block has a fairer chance of landing in
    // time, without removing the delay outright.
    public static final double DEFAULT_SHIELD_BLOCK_DELAY_MULTIPLIER = 0.4D;
    public static final double DEFAULT_BACKSTAB_DAMAGE_MULTIPLIER = 1.5D;
    // Full arc width, centered directly behind the target, that counts as "from behind".
    public static final int DEFAULT_BACKSTAB_CONE_DEGREES = 120;
    public static final boolean DEFAULT_BACKSTAB_VIGNETTE_ENABLED = true;
    // Vanilla's own sneaking reduction on mob detection range is only -20% (LivingEntity#isDiscrete
    // multiplies visibility by 0.8, confirmed via bytecode) — nowhere near enough for a real sneak
    // approach. This is an *additional* multiplier applied only when the player is sneaking AND
    // standing in the mob's blind spot (see ComboCombat#isBehind), on top of vanilla's own factor.
    public static final double DEFAULT_SNEAK_VISIBILITY_MULTIPLIER = 0.2D;
    // Close enough that it reads as "right behind them", not "anywhere in the blind spot".
    public static final double DEFAULT_TAKEDOWN_RANGE = 2.0D;
    // A real damage multiplier (routed through the normal hurt pipeline, so armor/invulnerability
    // still apply — no more bypassing a Creaking's damage immunity via a raw kill()), calibrated
    // so a wooden sword's ~4 base damage clears a zombie's 20 HP even after its ~2 armor points
    // (4 * 6 = 24, comfortably past ~21.7 needed after reduction). Tankier targets (Iron Golem's
    // 100 HP, Warden's 500 HP) take a serious hit but survive — that's the point, not a bug.
    public static final double DEFAULT_TAKEDOWN_DAMAGE_MULTIPLIER = 6.0D;
    public static final boolean DEFAULT_FULL_SNEAK_MODE_ENABLED = true;
    // Applies in every direction (not just the blind spot) while Full Sneak Mode is active,
    // stacked on top of vanilla's own 0.8x sneaking reduction — deliberately drastic, since the
    // blind-spot-only sneakVisibilityMultiplier above still let an off-center approach get made out.
    public static final double DEFAULT_FULL_SNEAK_OMNI_VISIBILITY_MULTIPLIER = 0.1D;
    public static final double DEFAULT_STAMINA_MAX = 100.0D;
    // 10/sec — a full-to-empty bar takes 10 seconds to recover once regen actually starts.
    public static final double DEFAULT_STAMINA_REGEN_PER_TICK = 0.5D;
    // 1 second of not sprinting/blocking/full-sneaking before regen kicks back in, so stamina
    // doesn't refill mid-fight the instant you let go of a key.
    public static final int DEFAULT_STAMINA_REGEN_DELAY_TICKS = 20;
    // Gradual drains — meant to be a slow background cost, not something that empties the bar in
    // a few seconds of ordinary play. A full bar lasts 25s of continuous sprinting or blocking.
    public static final double DEFAULT_STAMINA_SPRINT_DRAIN_PER_TICK = 0.2D;
    public static final double DEFAULT_STAMINA_BLOCK_DRAIN_PER_TICK = 0.2D;
    // Slower than sprint/block — Full Sneak Mode's benefit is meant to last a while, not evaporate
    // in a couple seconds, but it's still explicitly time-boxed rather than free forever.
    public static final double DEFAULT_STAMINA_FULL_SNEAK_DRAIN_PER_TICK = 0.4D;
    public static final double DEFAULT_STAMINA_DODGE_COST = 30.0D;
    // One-time spikes on top of the gradual drains above — a successful parry, a landed finisher,
    // and a silent takedown are all deliberate, high-value payoffs, so they cost noticeably more
    // than just standing there sprinting or blocking. These drain unconditionally (clamped at 0,
    // same as any other cost) rather than being gated on affordability like the dodge — a parry
    // or finisher still lands even on an empty bar, it just leaves you with nothing afterward.
    public static final double DEFAULT_STAMINA_PARRY_SPIKE = 15.0D;
    public static final double DEFAULT_STAMINA_FINISHER_SPIKE = 12.0D;
    public static final double DEFAULT_STAMINA_TAKEDOWN_SPIKE = 25.0D;
    public static final double DEFAULT_DODGE_STRENGTH = 0.9D;
    public static final int DEFAULT_DODGE_INVULNERABILITY_TICKS = 6;
    public static final int DEFAULT_DODGE_COOLDOWN_TICKS = 15;

    public static double minAttackStrength = DEFAULT_MIN_ATTACK_STRENGTH;
    public static int timeoutSlackTicks = DEFAULT_TIMEOUT_SLACK_TICKS;
    public static double finisherDamageMultiplier = DEFAULT_FINISHER_DAMAGE_MULTIPLIER;
    public static double finisherExtraKnockback = DEFAULT_FINISHER_EXTRA_KNOCKBACK;
    public static double axeKnockbackMultiplier = DEFAULT_AXE_KNOCKBACK_MULTIPLIER;
    public static double maceKnockbackMultiplier = DEFAULT_MACE_KNOCKBACK_MULTIPLIER;
    public static boolean damagePopupEnabled = DEFAULT_DAMAGE_POPUP_ENABLED;
    public static int staggerDurationTicks = DEFAULT_STAGGER_DURATION_TICKS;
    public static int staggerSlownessAmplifier = DEFAULT_STAGGER_SLOWNESS_AMPLIFIER;
    public static int staggerWeaknessAmplifier = DEFAULT_STAGGER_WEAKNESS_AMPLIFIER;
    public static boolean staggerVisualEnabled = DEFAULT_STAGGER_VISUAL_ENABLED;
    public static int swordBleedDurationTicks = DEFAULT_SWORD_BLEED_DURATION_TICKS;
    public static int swordBleedAmplifier = DEFAULT_SWORD_BLEED_AMPLIFIER;
    public static double maceAoeRadius = DEFAULT_MACE_AOE_RADIUS;
    public static double maceAoeDamage = DEFAULT_MACE_AOE_DAMAGE;
    public static int parryWindowTicks = DEFAULT_PARRY_WINDOW_TICKS;
    public static double parryKnockback = DEFAULT_PARRY_KNOCKBACK;
    public static int parryFinisherWindowTicks = DEFAULT_PARRY_FINISHER_WINDOW_TICKS;
    public static double shieldBlockDelayMultiplier = DEFAULT_SHIELD_BLOCK_DELAY_MULTIPLIER;
    public static double backstabDamageMultiplier = DEFAULT_BACKSTAB_DAMAGE_MULTIPLIER;
    public static int backstabConeDegrees = DEFAULT_BACKSTAB_CONE_DEGREES;
    public static boolean backstabVignetteEnabled = DEFAULT_BACKSTAB_VIGNETTE_ENABLED;
    public static double sneakVisibilityMultiplier = DEFAULT_SNEAK_VISIBILITY_MULTIPLIER;
    public static double takedownRange = DEFAULT_TAKEDOWN_RANGE;
    public static double takedownDamageMultiplier = DEFAULT_TAKEDOWN_DAMAGE_MULTIPLIER;
    public static boolean fullSneakModeEnabled = DEFAULT_FULL_SNEAK_MODE_ENABLED;
    public static double fullSneakOmniVisibilityMultiplier = DEFAULT_FULL_SNEAK_OMNI_VISIBILITY_MULTIPLIER;
    public static double staminaMax = DEFAULT_STAMINA_MAX;
    public static double staminaRegenPerTick = DEFAULT_STAMINA_REGEN_PER_TICK;
    public static int staminaRegenDelayTicks = DEFAULT_STAMINA_REGEN_DELAY_TICKS;
    public static double staminaSprintDrainPerTick = DEFAULT_STAMINA_SPRINT_DRAIN_PER_TICK;
    public static double staminaBlockDrainPerTick = DEFAULT_STAMINA_BLOCK_DRAIN_PER_TICK;
    public static double staminaFullSneakDrainPerTick = DEFAULT_STAMINA_FULL_SNEAK_DRAIN_PER_TICK;
    public static double staminaDodgeCost = DEFAULT_STAMINA_DODGE_COST;
    public static double staminaParrySpike = DEFAULT_STAMINA_PARRY_SPIKE;
    public static double staminaFinisherSpike = DEFAULT_STAMINA_FINISHER_SPIKE;
    public static double staminaTakedownSpike = DEFAULT_STAMINA_TAKEDOWN_SPIKE;
    public static double dodgeStrength = DEFAULT_DODGE_STRENGTH;
    public static int dodgeInvulnerabilityTicks = DEFAULT_DODGE_INVULNERABILITY_TICKS;
    public static int dodgeCooldownTicks = DEFAULT_DODGE_COOLDOWN_TICKS;

    private static SimpleConfig config;
    private static ComboConfigProvider provider;

    private ComboConfig() {
    }

    public static void registerConfigs() {
        LOGGER.info("Registering combo configuration.");
        provider = new ComboConfigProvider();

        provider.addKeyValuePair(new Pair<>(KEY_MIN_ATTACK_STRENGTH, DEFAULT_MIN_ATTACK_STRENGTH), Arrays.asList(
                "How charged (0.0-1.0) an attack must be to build or continue a combo.",
                "Default: " + DEFAULT_MIN_ATTACK_STRENGTH
        ));
        provider.addKeyValuePair(new Pair<>(KEY_TIMEOUT_SLACK_TICKS, DEFAULT_TIMEOUT_SLACK_TICKS), Arrays.asList(
                "Extra ticks (on top of the current weapon's own attack cooldown) allowed",
                "between hits before the combo resets.",
                "Default: " + DEFAULT_TIMEOUT_SLACK_TICKS
        ));
        provider.addKeyValuePair(new Pair<>(KEY_FINISHER_DAMAGE_MULTIPLIER, DEFAULT_FINISHER_DAMAGE_MULTIPLIER), Arrays.asList(
                "Damage multiplier applied to the 3rd (finisher) hit.",
                "Default: " + DEFAULT_FINISHER_DAMAGE_MULTIPLIER
        ));
        provider.addKeyValuePair(new Pair<>(KEY_FINISHER_EXTRA_KNOCKBACK, DEFAULT_FINISHER_EXTRA_KNOCKBACK), Arrays.asList(
                "Extra knockback strength applied on a finisher hit.",
                "Default: " + DEFAULT_FINISHER_EXTRA_KNOCKBACK
        ));
        provider.addKeyValuePair(new Pair<>(KEY_AXE_KNOCKBACK_MULTIPLIER, DEFAULT_AXE_KNOCKBACK_MULTIPLIER), Arrays.asList(
                "Multiplier applied on top of the finisher knockback when the weapon is an axe.",
                "Default: " + DEFAULT_AXE_KNOCKBACK_MULTIPLIER
        ));
        provider.addKeyValuePair(new Pair<>(KEY_MACE_KNOCKBACK_MULTIPLIER, DEFAULT_MACE_KNOCKBACK_MULTIPLIER), Arrays.asList(
                "Multiplier applied on top of the finisher knockback when the weapon is a mace.",
                "Default: " + DEFAULT_MACE_KNOCKBACK_MULTIPLIER
        ));
        provider.addKeyValuePair(new Pair<>(KEY_DAMAGE_POPUP_ENABLED, DEFAULT_DAMAGE_POPUP_ENABLED), Arrays.asList(
                "Whether floating damage numbers are shown above hit entities.",
                "This is a local display preference (read on whichever side you're playing on).",
                "Default: " + DEFAULT_DAMAGE_POPUP_ENABLED
        ));
        provider.addKeyValuePair(new Pair<>(KEY_STAGGER_DURATION_TICKS, DEFAULT_STAGGER_DURATION_TICKS), Arrays.asList(
                "How long (in ticks) a finisher staggers its target for.",
                "Default: " + DEFAULT_STAGGER_DURATION_TICKS
        ));
        provider.addKeyValuePair(new Pair<>(KEY_STAGGER_SLOWNESS_AMPLIFIER, DEFAULT_STAGGER_SLOWNESS_AMPLIFIER), Arrays.asList(
                "Slowness amplifier applied to a staggered target.",
                "Default: " + DEFAULT_STAGGER_SLOWNESS_AMPLIFIER
        ));
        provider.addKeyValuePair(new Pair<>(KEY_STAGGER_WEAKNESS_AMPLIFIER, DEFAULT_STAGGER_WEAKNESS_AMPLIFIER), Arrays.asList(
                "Weakness amplifier applied to a staggered target.",
                "Default: " + DEFAULT_STAGGER_WEAKNESS_AMPLIFIER
        ));
        provider.addKeyValuePair(new Pair<>(KEY_STAGGER_VISUAL_ENABLED, DEFAULT_STAGGER_VISUAL_ENABLED), Arrays.asList(
                "Whether staggered targets show an orbiting-star indicator.",
                "This is a local display preference (read on whichever side you're playing on).",
                "Default: " + DEFAULT_STAGGER_VISUAL_ENABLED
        ));
        provider.addKeyValuePair(new Pair<>(KEY_SWORD_BLEED_DURATION_TICKS, DEFAULT_SWORD_BLEED_DURATION_TICKS), Arrays.asList(
                "How long (in ticks) a sword finisher's bleed (Wither) effect lasts.",
                "Default: " + DEFAULT_SWORD_BLEED_DURATION_TICKS
        ));
        provider.addKeyValuePair(new Pair<>(KEY_SWORD_BLEED_AMPLIFIER, DEFAULT_SWORD_BLEED_AMPLIFIER), Arrays.asList(
                "Bleed (Wither) amplifier applied by a sword finisher.",
                "Default: " + DEFAULT_SWORD_BLEED_AMPLIFIER
        ));
        provider.addKeyValuePair(new Pair<>(KEY_MACE_AOE_RADIUS, DEFAULT_MACE_AOE_RADIUS), Arrays.asList(
                "Radius (in blocks) around the target that a mace finisher's splash damage reaches.",
                "Default: " + DEFAULT_MACE_AOE_RADIUS
        ));
        provider.addKeyValuePair(new Pair<>(KEY_MACE_AOE_DAMAGE, DEFAULT_MACE_AOE_DAMAGE), Arrays.asList(
                "Flat damage dealt to entities caught in a mace finisher's splash radius.",
                "Default: " + DEFAULT_MACE_AOE_DAMAGE
        ));
        provider.addKeyValuePair(new Pair<>(KEY_PARRY_WINDOW_TICKS, DEFAULT_PARRY_WINDOW_TICKS), Arrays.asList(
                "How recently you must have started blocking for a blocked hit to count as a parry.",
                "Default: " + DEFAULT_PARRY_WINDOW_TICKS
        ));
        provider.addKeyValuePair(new Pair<>(KEY_PARRY_KNOCKBACK, DEFAULT_PARRY_KNOCKBACK), Arrays.asList(
                "Knockback strength applied to an attacker on a successful parry.",
                "Default: " + DEFAULT_PARRY_KNOCKBACK
        ));
        provider.addKeyValuePair(new Pair<>(KEY_PARRY_FINISHER_WINDOW_TICKS, DEFAULT_PARRY_FINISHER_WINDOW_TICKS), Arrays.asList(
                "How long after a successful parry your next landed hit is auto-upgraded to a finisher.",
                "Default: " + DEFAULT_PARRY_FINISHER_WINDOW_TICKS
        ));
        provider.addKeyValuePair(new Pair<>(KEY_SHIELD_BLOCK_DELAY_MULTIPLIER, DEFAULT_SHIELD_BLOCK_DELAY_MULTIPLIER), Arrays.asList(
                "Multiplier applied to vanilla's own shield-raise windup before it starts blocking.",
                "1.0 is unchanged vanilla behavior, 0.0 blocks instantly.",
                "Default: " + DEFAULT_SHIELD_BLOCK_DELAY_MULTIPLIER
        ));
        provider.addKeyValuePair(new Pair<>(KEY_BACKSTAB_DAMAGE_MULTIPLIER, DEFAULT_BACKSTAB_DAMAGE_MULTIPLIER), Arrays.asList(
                "Damage multiplier applied when sneaking and hitting a target from behind.",
                "Default: " + DEFAULT_BACKSTAB_DAMAGE_MULTIPLIER
        ));
        provider.addKeyValuePair(new Pair<>(KEY_BACKSTAB_CONE_DEGREES, DEFAULT_BACKSTAB_CONE_DEGREES), Arrays.asList(
                "Width (in degrees) of the arc behind a target, centered directly behind it,",
                "within which a sneak attack counts as a backstab.",
                "Default: " + DEFAULT_BACKSTAB_CONE_DEGREES
        ));
        provider.addKeyValuePair(new Pair<>(KEY_BACKSTAB_VIGNETTE_ENABLED, DEFAULT_BACKSTAB_VIGNETTE_ENABLED), Arrays.asList(
                "Whether a subtle vignette shows while sneaking, flashing red on a successful backstab.",
                "This is a local display preference (read on whichever side you're playing on).",
                "Default: " + DEFAULT_BACKSTAB_VIGNETTE_ENABLED
        ));
        provider.addKeyValuePair(new Pair<>(KEY_SNEAK_VISIBILITY_MULTIPLIER, DEFAULT_SNEAK_VISIBILITY_MULTIPLIER), Arrays.asList(
                "Extra multiplier on a mob's detection range when you're sneaking in its blind spot,",
                "stacked on top of vanilla's own (mild) sneaking reduction. Lower is stealthier.",
                "Default: " + DEFAULT_SNEAK_VISIBILITY_MULTIPLIER
        ));
        provider.addKeyValuePair(new Pair<>(KEY_TAKEDOWN_RANGE, DEFAULT_TAKEDOWN_RANGE), Arrays.asList(
                "Distance (in blocks) within which a silent takedown can be performed on a target",
                "you're directly behind while Full Sneak Mode is active.",
                "Default: " + DEFAULT_TAKEDOWN_RANGE
        ));
        provider.addKeyValuePair(new Pair<>(KEY_TAKEDOWN_DAMAGE_MULTIPLIER, DEFAULT_TAKEDOWN_DAMAGE_MULTIPLIER), Arrays.asList(
                "Damage multiplier applied on a silent takedown hit. It's real damage through the",
                "normal pipeline (armor/invulnerability still apply), calibrated so weak hostiles",
                "die in one hit but tanky ones (Iron Golem, Warden) survive and take real damage.",
                "Default: " + DEFAULT_TAKEDOWN_DAMAGE_MULTIPLIER
        ));
        provider.addKeyValuePair(new Pair<>(KEY_FULL_SNEAK_MODE_ENABLED, DEFAULT_FULL_SNEAK_MODE_ENABLED), Arrays.asList(
                "Whether sneaking (actually crouching, no separate toggle) grants drastically",
                "reduced detection in every direction, not just a target's blind spot.",
                "Default: " + DEFAULT_FULL_SNEAK_MODE_ENABLED
        ));
        provider.addKeyValuePair(new Pair<>(KEY_FULL_SNEAK_OMNI_VISIBILITY_MULTIPLIER, DEFAULT_FULL_SNEAK_OMNI_VISIBILITY_MULTIPLIER), Arrays.asList(
                "Extra multiplier on a mob's detection range, in every direction, while sneaking",
                "with Full Sneak Mode enabled. Stacked on top of vanilla's own sneaking reduction.",
                "The blind spot is still stronger than this (full immunity). Lower is stealthier.",
                "Default: " + DEFAULT_FULL_SNEAK_OMNI_VISIBILITY_MULTIPLIER
        ));

        provider.addKeyValuePair(new Pair<>(KEY_STAMINA_MAX, DEFAULT_STAMINA_MAX), Arrays.asList(
                "Maximum stamina.",
                "Default: " + DEFAULT_STAMINA_MAX
        ));
        provider.addKeyValuePair(new Pair<>(KEY_STAMINA_REGEN_PER_TICK, DEFAULT_STAMINA_REGEN_PER_TICK), Arrays.asList(
                "Stamina regained per tick once regen is active.",
                "Default: " + DEFAULT_STAMINA_REGEN_PER_TICK
        ));
        provider.addKeyValuePair(new Pair<>(KEY_STAMINA_REGEN_DELAY_TICKS, DEFAULT_STAMINA_REGEN_DELAY_TICKS), Arrays.asList(
                "Ticks since the last stamina drain before regen starts.",
                "Default: " + DEFAULT_STAMINA_REGEN_DELAY_TICKS
        ));
        provider.addKeyValuePair(new Pair<>(KEY_STAMINA_SPRINT_DRAIN_PER_TICK, DEFAULT_STAMINA_SPRINT_DRAIN_PER_TICK), Arrays.asList(
                "Stamina drained per tick while sprinting. Sprinting is force-stopped at 0 stamina.",
                "Default: " + DEFAULT_STAMINA_SPRINT_DRAIN_PER_TICK
        ));
        provider.addKeyValuePair(new Pair<>(KEY_STAMINA_BLOCK_DRAIN_PER_TICK, DEFAULT_STAMINA_BLOCK_DRAIN_PER_TICK), Arrays.asList(
                "Stamina drained per tick while blocking. Blocking is force-stopped at 0 stamina.",
                "Default: " + DEFAULT_STAMINA_BLOCK_DRAIN_PER_TICK
        ));
        provider.addKeyValuePair(new Pair<>(KEY_STAMINA_FULL_SNEAK_DRAIN_PER_TICK, DEFAULT_STAMINA_FULL_SNEAK_DRAIN_PER_TICK), Arrays.asList(
                "Stamina drained per tick while Full Sneak Mode's detection bonus is active.",
                "The bonus itself stops applying at 0 stamina (you're still sneaking, just without it).",
                "Default: " + DEFAULT_STAMINA_FULL_SNEAK_DRAIN_PER_TICK
        ));
        provider.addKeyValuePair(new Pair<>(KEY_STAMINA_DODGE_COST, DEFAULT_STAMINA_DODGE_COST), Arrays.asList(
                "Flat stamina cost to perform a dodge. Not enough stamina means the dodge fails.",
                "Default: " + DEFAULT_STAMINA_DODGE_COST
        ));
        provider.addKeyValuePair(new Pair<>(KEY_STAMINA_PARRY_SPIKE, DEFAULT_STAMINA_PARRY_SPIKE), Arrays.asList(
                "One-time stamina cost on a successful parry. Drains unconditionally, doesn't block the parry.",
                "Default: " + DEFAULT_STAMINA_PARRY_SPIKE
        ));
        provider.addKeyValuePair(new Pair<>(KEY_STAMINA_FINISHER_SPIKE, DEFAULT_STAMINA_FINISHER_SPIKE), Arrays.asList(
                "One-time stamina cost on a landed finisher hit. Drains unconditionally, doesn't block the hit.",
                "Default: " + DEFAULT_STAMINA_FINISHER_SPIKE
        ));
        provider.addKeyValuePair(new Pair<>(KEY_STAMINA_TAKEDOWN_SPIKE, DEFAULT_STAMINA_TAKEDOWN_SPIKE), Arrays.asList(
                "One-time stamina cost on a successful silent takedown. Drains unconditionally, doesn't block it.",
                "Default: " + DEFAULT_STAMINA_TAKEDOWN_SPIKE
        ));
        provider.addKeyValuePair(new Pair<>(KEY_DODGE_STRENGTH, DEFAULT_DODGE_STRENGTH), Arrays.asList(
                "Horizontal impulse strength applied by a dodge.",
                "Default: " + DEFAULT_DODGE_STRENGTH
        ));
        provider.addKeyValuePair(new Pair<>(KEY_DODGE_INVULNERABILITY_TICKS, DEFAULT_DODGE_INVULNERABILITY_TICKS), Arrays.asList(
                "How long (in ticks) a dodge grants full damage immunity for.",
                "Default: " + DEFAULT_DODGE_INVULNERABILITY_TICKS
        ));
        provider.addKeyValuePair(new Pair<>(KEY_DODGE_COOLDOWN_TICKS, DEFAULT_DODGE_COOLDOWN_TICKS), Arrays.asList(
                "Minimum ticks between dodges, independent of stamina.",
                "Default: " + DEFAULT_DODGE_COOLDOWN_TICKS
        ));

        config = SimpleConfig.of("comboconfig").provider(provider).request();
        assignConfigs();
    }

    private static void assignConfigs() {
        minAttackStrength = config.getOrDefault(KEY_MIN_ATTACK_STRENGTH, DEFAULT_MIN_ATTACK_STRENGTH);
        timeoutSlackTicks = config.getOrDefault(KEY_TIMEOUT_SLACK_TICKS, DEFAULT_TIMEOUT_SLACK_TICKS);
        finisherDamageMultiplier = config.getOrDefault(KEY_FINISHER_DAMAGE_MULTIPLIER, DEFAULT_FINISHER_DAMAGE_MULTIPLIER);
        finisherExtraKnockback = config.getOrDefault(KEY_FINISHER_EXTRA_KNOCKBACK, DEFAULT_FINISHER_EXTRA_KNOCKBACK);
        axeKnockbackMultiplier = config.getOrDefault(KEY_AXE_KNOCKBACK_MULTIPLIER, DEFAULT_AXE_KNOCKBACK_MULTIPLIER);
        maceKnockbackMultiplier = config.getOrDefault(KEY_MACE_KNOCKBACK_MULTIPLIER, DEFAULT_MACE_KNOCKBACK_MULTIPLIER);
        damagePopupEnabled = config.getOrDefault(KEY_DAMAGE_POPUP_ENABLED, DEFAULT_DAMAGE_POPUP_ENABLED);
        staggerDurationTicks = config.getOrDefault(KEY_STAGGER_DURATION_TICKS, DEFAULT_STAGGER_DURATION_TICKS);
        staggerSlownessAmplifier = config.getOrDefault(KEY_STAGGER_SLOWNESS_AMPLIFIER, DEFAULT_STAGGER_SLOWNESS_AMPLIFIER);
        staggerWeaknessAmplifier = config.getOrDefault(KEY_STAGGER_WEAKNESS_AMPLIFIER, DEFAULT_STAGGER_WEAKNESS_AMPLIFIER);
        staggerVisualEnabled = config.getOrDefault(KEY_STAGGER_VISUAL_ENABLED, DEFAULT_STAGGER_VISUAL_ENABLED);
        swordBleedDurationTicks = config.getOrDefault(KEY_SWORD_BLEED_DURATION_TICKS, DEFAULT_SWORD_BLEED_DURATION_TICKS);
        swordBleedAmplifier = config.getOrDefault(KEY_SWORD_BLEED_AMPLIFIER, DEFAULT_SWORD_BLEED_AMPLIFIER);
        maceAoeRadius = config.getOrDefault(KEY_MACE_AOE_RADIUS, DEFAULT_MACE_AOE_RADIUS);
        maceAoeDamage = config.getOrDefault(KEY_MACE_AOE_DAMAGE, DEFAULT_MACE_AOE_DAMAGE);
        parryWindowTicks = config.getOrDefault(KEY_PARRY_WINDOW_TICKS, DEFAULT_PARRY_WINDOW_TICKS);
        parryKnockback = config.getOrDefault(KEY_PARRY_KNOCKBACK, DEFAULT_PARRY_KNOCKBACK);
        parryFinisherWindowTicks = config.getOrDefault(KEY_PARRY_FINISHER_WINDOW_TICKS, DEFAULT_PARRY_FINISHER_WINDOW_TICKS);
        shieldBlockDelayMultiplier = config.getOrDefault(KEY_SHIELD_BLOCK_DELAY_MULTIPLIER, DEFAULT_SHIELD_BLOCK_DELAY_MULTIPLIER);
        backstabDamageMultiplier = config.getOrDefault(KEY_BACKSTAB_DAMAGE_MULTIPLIER, DEFAULT_BACKSTAB_DAMAGE_MULTIPLIER);
        backstabConeDegrees = config.getOrDefault(KEY_BACKSTAB_CONE_DEGREES, DEFAULT_BACKSTAB_CONE_DEGREES);
        backstabVignetteEnabled = config.getOrDefault(KEY_BACKSTAB_VIGNETTE_ENABLED, DEFAULT_BACKSTAB_VIGNETTE_ENABLED);
        sneakVisibilityMultiplier = config.getOrDefault(KEY_SNEAK_VISIBILITY_MULTIPLIER, DEFAULT_SNEAK_VISIBILITY_MULTIPLIER);
        takedownRange = config.getOrDefault(KEY_TAKEDOWN_RANGE, DEFAULT_TAKEDOWN_RANGE);
        takedownDamageMultiplier = config.getOrDefault(KEY_TAKEDOWN_DAMAGE_MULTIPLIER, DEFAULT_TAKEDOWN_DAMAGE_MULTIPLIER);
        fullSneakModeEnabled = config.getOrDefault(KEY_FULL_SNEAK_MODE_ENABLED, DEFAULT_FULL_SNEAK_MODE_ENABLED);
        fullSneakOmniVisibilityMultiplier = config.getOrDefault(KEY_FULL_SNEAK_OMNI_VISIBILITY_MULTIPLIER, DEFAULT_FULL_SNEAK_OMNI_VISIBILITY_MULTIPLIER);
        staminaMax = config.getOrDefault(KEY_STAMINA_MAX, DEFAULT_STAMINA_MAX);
        staminaRegenPerTick = config.getOrDefault(KEY_STAMINA_REGEN_PER_TICK, DEFAULT_STAMINA_REGEN_PER_TICK);
        staminaRegenDelayTicks = config.getOrDefault(KEY_STAMINA_REGEN_DELAY_TICKS, DEFAULT_STAMINA_REGEN_DELAY_TICKS);
        staminaSprintDrainPerTick = config.getOrDefault(KEY_STAMINA_SPRINT_DRAIN_PER_TICK, DEFAULT_STAMINA_SPRINT_DRAIN_PER_TICK);
        staminaBlockDrainPerTick = config.getOrDefault(KEY_STAMINA_BLOCK_DRAIN_PER_TICK, DEFAULT_STAMINA_BLOCK_DRAIN_PER_TICK);
        staminaFullSneakDrainPerTick = config.getOrDefault(KEY_STAMINA_FULL_SNEAK_DRAIN_PER_TICK, DEFAULT_STAMINA_FULL_SNEAK_DRAIN_PER_TICK);
        staminaDodgeCost = config.getOrDefault(KEY_STAMINA_DODGE_COST, DEFAULT_STAMINA_DODGE_COST);
        staminaParrySpike = config.getOrDefault(KEY_STAMINA_PARRY_SPIKE, DEFAULT_STAMINA_PARRY_SPIKE);
        staminaFinisherSpike = config.getOrDefault(KEY_STAMINA_FINISHER_SPIKE, DEFAULT_STAMINA_FINISHER_SPIKE);
        staminaTakedownSpike = config.getOrDefault(KEY_STAMINA_TAKEDOWN_SPIKE, DEFAULT_STAMINA_TAKEDOWN_SPIKE);
        dodgeStrength = config.getOrDefault(KEY_DODGE_STRENGTH, DEFAULT_DODGE_STRENGTH);
        dodgeInvulnerabilityTicks = config.getOrDefault(KEY_DODGE_INVULNERABILITY_TICKS, DEFAULT_DODGE_INVULNERABILITY_TICKS);
        dodgeCooldownTicks = config.getOrDefault(KEY_DODGE_COOLDOWN_TICKS, DEFAULT_DODGE_COOLDOWN_TICKS);
        LOGGER.info("Combo configuration loaded.");
    }

    public static void saveConfigs() {
        config.set(KEY_MIN_ATTACK_STRENGTH, minAttackStrength);
        config.set(KEY_TIMEOUT_SLACK_TICKS, timeoutSlackTicks);
        config.set(KEY_FINISHER_DAMAGE_MULTIPLIER, finisherDamageMultiplier);
        config.set(KEY_FINISHER_EXTRA_KNOCKBACK, finisherExtraKnockback);
        config.set(KEY_AXE_KNOCKBACK_MULTIPLIER, axeKnockbackMultiplier);
        config.set(KEY_MACE_KNOCKBACK_MULTIPLIER, maceKnockbackMultiplier);
        config.set(KEY_DAMAGE_POPUP_ENABLED, damagePopupEnabled);
        config.set(KEY_STAGGER_DURATION_TICKS, staggerDurationTicks);
        config.set(KEY_STAGGER_SLOWNESS_AMPLIFIER, staggerSlownessAmplifier);
        config.set(KEY_STAGGER_WEAKNESS_AMPLIFIER, staggerWeaknessAmplifier);
        config.set(KEY_STAGGER_VISUAL_ENABLED, staggerVisualEnabled);
        config.set(KEY_SWORD_BLEED_DURATION_TICKS, swordBleedDurationTicks);
        config.set(KEY_SWORD_BLEED_AMPLIFIER, swordBleedAmplifier);
        config.set(KEY_MACE_AOE_RADIUS, maceAoeRadius);
        config.set(KEY_MACE_AOE_DAMAGE, maceAoeDamage);
        config.set(KEY_PARRY_WINDOW_TICKS, parryWindowTicks);
        config.set(KEY_PARRY_KNOCKBACK, parryKnockback);
        config.set(KEY_PARRY_FINISHER_WINDOW_TICKS, parryFinisherWindowTicks);
        config.set(KEY_SHIELD_BLOCK_DELAY_MULTIPLIER, shieldBlockDelayMultiplier);
        config.set(KEY_BACKSTAB_DAMAGE_MULTIPLIER, backstabDamageMultiplier);
        config.set(KEY_BACKSTAB_CONE_DEGREES, backstabConeDegrees);
        config.set(KEY_BACKSTAB_VIGNETTE_ENABLED, backstabVignetteEnabled);
        config.set(KEY_SNEAK_VISIBILITY_MULTIPLIER, sneakVisibilityMultiplier);
        config.set(KEY_TAKEDOWN_RANGE, takedownRange);
        config.set(KEY_TAKEDOWN_DAMAGE_MULTIPLIER, takedownDamageMultiplier);
        config.set(KEY_FULL_SNEAK_MODE_ENABLED, fullSneakModeEnabled);
        config.set(KEY_FULL_SNEAK_OMNI_VISIBILITY_MULTIPLIER, fullSneakOmniVisibilityMultiplier);
        config.set(KEY_STAMINA_MAX, staminaMax);
        config.set(KEY_STAMINA_REGEN_PER_TICK, staminaRegenPerTick);
        config.set(KEY_STAMINA_REGEN_DELAY_TICKS, staminaRegenDelayTicks);
        config.set(KEY_STAMINA_SPRINT_DRAIN_PER_TICK, staminaSprintDrainPerTick);
        config.set(KEY_STAMINA_BLOCK_DRAIN_PER_TICK, staminaBlockDrainPerTick);
        config.set(KEY_STAMINA_FULL_SNEAK_DRAIN_PER_TICK, staminaFullSneakDrainPerTick);
        config.set(KEY_STAMINA_DODGE_COST, staminaDodgeCost);
        config.set(KEY_STAMINA_PARRY_SPIKE, staminaParrySpike);
        config.set(KEY_STAMINA_FINISHER_SPIKE, staminaFinisherSpike);
        config.set(KEY_STAMINA_TAKEDOWN_SPIKE, staminaTakedownSpike);
        config.set(KEY_DODGE_STRENGTH, dodgeStrength);
        config.set(KEY_DODGE_INVULNERABILITY_TICKS, dodgeInvulnerabilityTicks);
        config.set(KEY_DODGE_COOLDOWN_TICKS, dodgeCooldownTicks);

        List<String> necessaryConfigs = provider.keys();
        config.save(necessaryConfigs.toArray(new String[0]));
        LOGGER.info("Combo configuration saved.");
    }
}
