# Combo — project summary (resume-from-here doc)

This file exists so a new session (Claude or otherwise) can pick this project up cold. Read this
before touching code. It covers what exists, how it's built, why key decisions were made, and
what's still loose. `docs/description.md` is the user-facing pitch (Modrinth copy); this is the
internal one.

## What this is

An RPG-style melee combat overhaul for Minecraft, built as an Architectury mod (Fabric + NeoForge)
targeting **Minecraft 26.1.2** (yes, that version number is real for this codebase — don't assume
it's a typo for 1.26 or similar). Package root: `net.vg.combo`. Mod ID: `combo`.

Versions (from `gradle.properties`):
| Component | Version |
| --- | --- |
| Minecraft | 26.1.2 |
| Java | 25 |
| Architectury API | 20.0.7 |
| Fabric API | 0.153.0+26.1.2 |
| NeoForge | 26.1.2.76 |

Build/run:
```
cmd.exe /c "python build-smart.py compile"        # compile-only, fast, run after every change
```
`:fabric:runClient` (via Gradle) launches the dev client for manual testing — this project has no
automated test suite; every feature so far has been verified by hand in a running client.

## Architecture

Standard Architectury 3-module layout:
- `common/` — all real logic. Mixins, config, network payloads, client rendering code (yes, client
  code lives in `common` too — see below), everything.
- `fabric/` — Fabric entrypoints only (`ComboFabric`, `ComboFabricClient`, `ComboModMenu`).
- `neoforge/` — NeoForge entrypoints only (`ComboNeoForge`, `ComboNeoForgeRenderHooks`).

Both loader modules are thin: they call into `Combo.init()` / `Combo.initClient()` in `common` and
wire loader-specific render-hook events (`LevelRenderEvents.BEFORE_GIZMOS` on Fabric,
`RenderLevelStageEvent.AfterLevel` on NeoForge) to the same shared drain/render methods. There is
**no loader-specific gameplay logic anywhere** — if you're about to write something in `fabric/` or
`neoforge/` beyond entrypoint boilerplate, that's very likely a sign it belongs in `common/`.

Client-only code (HUD, vignette, keybinds, client-side state mirrors) lives under
`common/src/main/java/net/vg/combo/client/` — this compiles fine because Architectury's `common`
source set has client-side Minecraft classes on the classpath; it's just never touched from
`Combo.init()` (server-safe), only from `Combo.initClient()` (physical-client-only, called from each
loader's client entrypoint).

### Package layout
```
net.vg.combo/
  Combo.java                    — central registration: payloads, tick hooks, cleanup, init/initClient
  combat/                       — all server-authoritative gameplay state & logic
    ComboCombat.java            — combo/finisher state machine, isBehind() geometry, feedback effects
    ComboState.java             — per-player combo progress (stage, timing, parry-finisher arm)
    ComboParry.java             — block-start tracking, parry judgement, parry payoff
    ComboStamina.java           — per-player stamina, drain/regen/spike accounting
    ComboDodge.java             — dodge execution, cooldown, invulnerability window
    WeaponCategory.java         — SWORD/AXE/MACE/OTHER resolution + per-category finisher sound
  config/
    ComboConfig.java            — every tunable constant: KEY_*, DEFAULT_*, live field, register/assign/save
    ComboConfigProvider.java, SimpleConfig.java  — pre-existing config plumbing (not touched this session)
  mixin/                        — all bytecode injection points, see "Mixins" below
  network/                      — CustomPacketPayload records, one file each
  client/                       — HUD, vignette, client-side mirrors of server state, keybinds
  stats/
    ComboStats.java             — custom vanilla stats (hits_landed, finishers_landed, damage_dealt)
```

## Feature inventory (what actually exists right now)

### 1. Combo & finishers
- `ComboCombat.prepareAttack()` / `confirmAttack()`, driven from `PlayerAttackMixin`'s `@Redirect`
  on `Entity#hurtOrSimulate` inside `Player#attack`.
- 3-hit chain (`COMBO_LENGTH = 3`, structural, not a config value), timeout = weapon's own attack
  cooldown + `timeoutSlackTicks`. Requires `attackStrength >= minAttackStrength` to count.
- 3rd hit = finisher: `finisherDamageMultiplier`, `finisherExtraKnockback` (scaled further by
  `axeKnockbackMultiplier`/`maceKnockbackMultiplier` per `WeaponCategory`), stagger (Slowness +
  Weakness via `ComboCombat.applyStagger`), sword-only bleed (Wither effect), mace-only AoE splash.
- Client HUD: pips (`ComboHud`), synced via `ComboSyncPayload` (S2C).

### 2. Parry & counter
- `LivingEntityBlockMixin` injects `LivingEntity#blockedByItem` at HEAD. **Important gotcha**:
  this fires ON the attacker with the defender as the parameter — i.e. `attacker.blockedByItem(defender)`
  — the opposite of what you'd naively assume. Confirmed via careful bytecode stack-order reading;
  this was a real bug earlier in development. If you ever touch this mixin, re-verify with `javap`,
  don't trust intuition.
- `ComboParry.trackBlockState()` polls `player.isBlocking()` every tick (`TickEvent.PLAYER_POST`)
  to record `BLOCK_START_TICK`. A hit blocked within `parryWindowTicks` of that start = a parry.
- Parry payoff: knockback (`parryKnockback`), stagger, arms `parryFinisherWindowTicks` of
  "next landed hit is an outright finisher" (`ComboCombat.armParryFinisher`), stamina spike
  (`staminaParrySpike`).
- `BlocksAttacksMixin` scales vanilla's own shield-raise windup (`BlocksAttacks#blockDelayTicks`,
  5 ticks default) by `shieldBlockDelayMultiplier` — without this, a "well-timed" block often can't
  even start blocking in time to be judged as a parry at all.

### 3. Stealth, backstab, Full Sneak Mode, silent takedown
- `ComboCombat.isBehind(mover, facer)` — shared geometry (horizontal look-angle dot product vs. a
  configurable cone) used by backstab, `StealthVisibilityMixin`, and the takedown eligibility check.
  This is the one function to know if you're touching any stealth-adjacent code.
- **Backstab**: sneaking + `isBehind` + hitting a `LivingEntity` → `backstabDamageMultiplier`
  (composes multiplicatively with the combo multiplier), `ComboCombat.applyBackstabFeedback` (sound
  + `ComboBackstabPayload` S2C → triggers `ComboVignetteEffects.trigger()`, a brief red flash).
- **Vignette** (`ComboVignetteEffects`): persistent subtle black border the entire time
  `Minecraft.player.isCrouching()`, separate brief red flash layered on top on a backstab. Built with
  `GuiGraphicsExtractor.fill()` only — there's no gradient/shader primitive available, so it's an
  approximated border, not a true vignette.
- **`StealthVisibilityMixin`**: injects `LivingEntity#getVisibilityPercent(Entity looker)` at
  RETURN, cancellable. This is what `TargetingConditions#test` (mob target-acquisition) multiplies
  detection range by. Vanilla's own sneaking reduction there is only **0.8x** (`isDiscrete()`,
  confirmed via bytecode) — nowhere near enough for a real stealth mechanic on its own. Logic:
  - If `ComboStamina.isFullSneakActive(player)` (= `fullSneakModeEnabled` config on **and**
    `player.isDiscrete()` **and** stamina > 0): full immunity (`0.0`) if `isBehind`, else
    `fullSneakOmniVisibilityMultiplier` in every other direction.
  - Else if just sneaking + `isBehind`: the older, weaker `sneakVisibilityMultiplier` (blind-spot
    only). This is the fallback behavior when Full Sneak Mode is off.
- **Full Sneak Mode is deliberately *not* a separate toggle/flag** — it's just
  `fullSneakModeEnabled && player.isDiscrete() && stamina > 0`, recomputed live every check. This
  was a deliberate redesign after an earlier version used a custom keybind + C2S/S2C toggle payloads
  + a server-side "active" flag decoupled from real crouch state — the user explicitly rejected that
  as inconsistent ("it's supposed to be a sneak, not a separate flag you can desync"). **Do not
  reintroduce a decoupled toggle for this** unless explicitly asked to.
- **Silent takedown** (`PlayerAttackMixin.combo$isTakedownEligible` / the takedown branch in
  `combo$redirectAttackDamage`): eligible when Full Sneak Mode is active, target is a `Monster`,
  `IronGolem`, or `AbstractVillager` (note: `net.minecraft.world.entity.npc.villager.AbstractVillager`
  and `net.minecraft.world.entity.animal.golem.IronGolem` — both moved packages in this MC version,
  confirmed via jar extraction, don't trust older-version import paths), **not** `WitherBoss`/
  `EnderDragon`, **not already targeting the attacking player** (`mob.getTarget() == serverPlayer`
  disqualifies it — a takedown only works on something that hasn't noticed you), `isBehind`, and
  within `takedownRange`.
  - **Important history**: this used to be a hard `LivingEntity#kill(ServerLevel)` call (same
    mechanism as `/kill`). That was **wrong** — it bypassed damage-immunity mechanics entirely (a
    Creaking, which is normally near-unkillable outside destroying its heart, could be instantly
    deleted). It was changed to a large real damage multiplier (`takedownDamageMultiplier`, default
    6.0x) applied through the *normal* `hurtOrSimulate` call, same pipeline as every other hit — so
    armor, invulnerability, and any damage-immunity logic on the target still apply. Fodder still
    dies in one hit; genuinely tough things (Iron Golem, Warden) take a serious hit and live. **If
    you're ever tempted to use `kill()` for an "instakill" mechanic again, don't — route it through
    real damage instead.**
  - Stamina spike on success: `staminaTakedownSpike`.

### 4. Stamina & dodge
- `ComboStamina`: `Map<UUID, Float> CURRENT` + `Map<UUID, Long> LAST_DRAIN_TICK`, ticked once per
  player per server tick from the same `TickEvent.PLAYER_POST` hook everything else uses.
  - Gradual drains: sprinting (`staminaSprintDrainPerTick`) and blocking (`staminaBlockDrainPerTick`)
    — both force-cut (`setSprinting(false)` / `stopUsingItem()`) the instant stamina hits 0, checked
    *before* draining further (never goes negative, denies the action outright rather than allowing
    "one more tick"). Full Sneak Mode's bonus also drains (`staminaFullSneakDrainPerTick`) but isn't
    force-cut — it just stops being eligible once stamina is empty (see above).
  - Regen: `staminaRegenPerTick`, only after `staminaRegenDelayTicks` of no drain at all.
  - Two different spend methods, used deliberately differently:
    - `consume(ServerPlayer, float)` — **affordability-gated**, returns false (and does nothing) if
      unaffordable. Used by dodge — a dodge you can't afford just doesn't happen.
    - `drainSpike(ServerPlayer, float)` — **unconditional**, clamps at 0, always "succeeds". Used by
      a parry, a landed finisher, and a takedown — these are payoffs for skill, not things that
      should get denied by low stamina; they just leave you emptier afterward.
- `ComboDodge`: directional dash + brief total invulnerability (`Entity#setInvulnerable(true)` for
  `dodgeInvulnerabilityTicks`, same flag creative players/spawn protection use), gated by
  `staminaDodgeCost` (via `consume`) and its own `dodgeCooldownTicks` independent of stamina.
  - **Two real bugs fixed here, both worth knowing about if dodge is ever touched again**:
    1. **Direction was always backward.** `Entity#getDeltaMovement()` does *not* reflect real WASD
       input for a `ServerPlayer` — player movement is packet/position-driven server-side, not
       physics-simulated the way mob AI is, so it reads as ~0 even while visibly walking. Fixed by
       using `ServerPlayer#getLastClientMoveIntent()` instead — confirmed via bytecode to be the
       real held-key direction, already rotated into world space by the player's yaw via the same
       `Entity#getInputVector()` routine vanilla's own movement code uses internally.
    2. **The dash never visibly moved the player.** A server-side `Entity#push()`/`setDeltaMovement()`
       on a `ServerPlayer` silently does nothing from that player's own perspective — the standard
       tracked-entity sync deliberately does not send a player motion updates about *their own*
       entity (the client is assumed authoritative over its own movement). Fixed by explicitly
       sending `player.connection.send(new ClientboundSetEntityMotionPacket(player))` right after
       the `push()` call. **This is the general pattern for any future "shove the player" effect —
       always pair a server-side player velocity change with an explicit motion packet.**
- Client HUD: `ComboHud` renders a small bar (`ComboStaminaClient`, synced via `ComboStaminaPayload`
  S2C) only while not full. Positioned at `guiHeight() - PIP_Y_OFFSET(78)` for the combo pips and
  `guiHeight() - STAMINA_BAR_Y_OFFSET(68)` for the stamina bar, stamina directly below the pips with
  a small gap — this exact layout went through several rounds of user correction to avoid
  overlapping vanilla's armor bar and hotbar item-name tooltips; if you reposition this HUD again,
  the derivation is `PIP_Y_OFFSET = STAMINA_BAR_Y_OFFSET + GAP_BETWEEN_ROWS` in `ComboHud.java` —
  keep that relationship intact rather than hand-tuning both numbers independently.

## Config system pattern

Every tunable follows the exact same 6-step shape in `ComboConfig.java` — copy this pattern exactly
for any new tunable, don't improvise a variant:
1. `private static final String KEY_X = "combo.x";`
2. `public static final <type> DEFAULT_X = <value>;` (with a comment explaining *why* that default,
   if it's not self-evident)
3. `public static <type> x = DEFAULT_X;` (the live, mutable value everything else reads)
4. In `registerConfigs()`: `provider.addKeyValuePair(new Pair<>(KEY_X, DEFAULT_X), Arrays.asList(...));`
   (human-readable comment lines, shown in the on-disk config file)
5. In `assignConfigs()`: `x = config.getOrDefault(KEY_X, DEFAULT_X);`
6. In `saveConfigs()`: `config.set(KEY_X, x);`

Every config value also gets a mirrored row in `ComboConfigScreen.java` (a `doubleOption`/`intOption`/
`booleanOption` helper call, added to the `options` array) and two lines in
`common/src/main/resources/assets/combo/lang/en_us.json` (`combo.x` label +
`tooltip.combo.x` tooltip). **A config value that's missing any one of these five places (constant
key, default+field, provider registration, screen entry, lang entry) is an incomplete change** —
this has been the discipline all along; don't skip the config screen or lang entries even for a
"quick" tunable.

## Mixins (`common/src/main/resources/combo.mixins.json`)

Currently registered: `PlayerAttackMixin`, `LivingEntityBlockMixin`, `BlocksAttacksMixin`,
`StealthVisibilityMixin`. All four target vanilla classes and were built by disassembling the exact
mapped bytecode (`javap -c -p -classpath <merged-jar>`) before writing the injection — **never
assume a vanilla method's parameter order, return semantics, or call-site position from memory or
from older-Minecraft-version knowledge**. The merged jar to check against:
```
/mnt/c/Users/video/.gradle/caches/fabric-loom/26.1.2/minecraft-merged.jar
```
(Architectury dev jar for cross-loader API surface, e.g. `NetworkManager`, `KeyMappingRegistry`:
`/mnt/c/Users/video/.gradle/caches/modules-2/files-2.1/dev.architectury/architectury/20.0.7/...`)

A removed mixin worth knowing about: `MeleeAttackGoalMixin` (an incoming-attack "telegraph" warning
above hostile mobs, Batman-Arkham-style). It was fully built, worked correctly after several rounds
of fixing (geometry, timing relative to `checkAndPerformAttack()`), and then **explicitly removed
at the user's request** — they found it made the parry system feel more confusing, not less, even
once it worked as designed. **Do not rebuild this feature unless explicitly asked** — it was a
deliberate rejection of the concept, not an unfinished attempt.

## Networking pattern

Every payload is a `record` implementing `CustomPacketPayload`, in its own file under `network/`,
following this exact shape (see e.g. `ComboStaminaPayload.java` for a 2-field S2C example,
`ComboDodgeRequestPayload.java` for a zero-field C2S trigger via `StreamCodec.unit(...)`):
```java
public record ComboXPayload(...) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ComboXPayload> TYPE = new CustomPacketPayload.Type<>(
        Identifier.fromNamespaceAndPath(Combo.MOD_ID, "x"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ComboXPayload> STREAM_CODEC =
        StreamCodec.composite(ByteBufCodecs.FLOAT, ComboXPayload::field, ..., ComboXPayload::new);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
```
Registered once each in `Combo.init()` via `NetworkManager.registerReceiver(NetworkManager.s2c()/c2s(),
Payload.TYPE, Payload.STREAM_CODEC, (payload, context) -> context.queue(() -> ...))` —
`context.queue()` is required to defer onto the main thread regardless of which side is receiving.
`ByteBufCodecs.BOOL`/`FLOAT`/`VAR_INT` etc. are `StreamCodec<ByteBuf, T>`, which is accepted by
`composite`'s `StreamCodec<? super RegistryFriendlyByteBuf, T>` bound since `RegistryFriendlyByteBuf`
is-a `ByteBuf` — this is intentional and confirmed working, not a type-system workaround.

## Server-side per-player state pattern

`ComboCombat`, `ComboParry`, `ComboStamina`, `ComboDodge` all use the same shape: one or more
`private static final Map<UUID, X>` fields, no persistence, cleared via `PlayerEvent.PLAYER_QUIT`
and `PlayerEvent.PLAYER_RESPAWN` registered in `Combo.init()`. **Any new stateful mechanic needs its
`::clear` method added to both of those registration blocks in `Combo.init()`** — this has been
missed before and is an easy thing to forget when adding a new system.

## Known loose ends / not-yet-done

- **`fabric.mod.json` and `neoforge.mods.toml` still have placeholder metadata**: description says
  "Starter Architectury mod template for Minecraft 26.1.2." and author is "Replace Me". These were
  never updated during feature development — worth fixing before any real Modrinth publish, using
  `docs/description.md` as the source for the real description.
- **No automated tests.** Every feature has been verified by hand in `:fabric:runClient`. NeoForge
  has *not* been manually tested this session — all testing was Fabric-only. Nothing loader-specific
  was touched, so it should work, but this is unverified, not confirmed.
- **`docs/v2-potential-plan.md`** has a running brainstorm list of not-yet-built ideas (combo streak
  counter, perfect-dodge window, armor-weight-affects-dodge, posture/break meter, Estus-alike heal
  item, hit-stop/freeze-frame on finishers). Hyperarmor/poise was floated and explicitly walked back
  — vanilla Minecraft has no concept of interrupting an attack mid-swing at all, so it would require
  inventing that mechanic from scratch first; not a good next step as originally framed.
- **Combo streak counter** (Arkham-style: a session-level "kills/hits without interruption" meter,
  distinct from the existing 3-hit-to-finisher chain, escalating rewards as it climbs) was the most
  recent concrete idea discussed but not built.

## Working discipline (how this codebase was built — keep doing this)

- **Verify every non-trivial vanilla API/behavior via `javap -c -p` against the merged jar before
  writing code that depends on it.** This project's history includes multiple real bugs that were
  only caught this way (the `blockedByItem` attacker/defender parameter order, the dodge
  direction/motion-sync issues, the `getVisibilityPercent` multiplier semantics). Do not guess from
  older-Minecraft-version knowledge or general modding intuition — this specific version's package
  layout has moved classes around (`Zombie`, `IronGolem`, `Villager`/`AbstractVillager`, `Warden` all
  live in non-obvious packages compared to older versions).
- **Every config value gets the full 5-place treatment** (see Config section above) — no exceptions
  for "just testing" values.
- **When the user says "remove this feature," remove it fully** — delete the files, not just
  disable them, and grep for stray references afterward (`grep -rn "ClassName"`) before compiling.
- **Compile after every coherent change**, not in one big batch:
  `cmd.exe /c "python build-smart.py compile"`.
- **Damage effects route through the real damage pipeline** (`hurtOrSimulate`/`hurt`), never a raw
  `kill()` or other bypass — see the takedown history above for exactly why this matters.
