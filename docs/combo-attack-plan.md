# Combo: implementation handoff plan

## Goal

Implement a small, server-authoritative melee combo system for Minecraft 26.1.2:

- A player builds a combo by landing timely, sufficiently charged main-hand melee attacks.
- A chain expires after a short timeout (recommended: 12 game ticks / 0.6 seconds).
- The third confirmed hit is a finisher: it deals a configurable damage multiplier and adds a stronger knockback plus visible/audible feedback.
- The player receives a compact client HUD indication of the current stage and timeout.

This is an Architectury project. Shared gameplay logic and the required mixin belong in `common`; Fabric and NeoForge should only contain bootstrap/client registration and their metadata declarations.

## Verified project and library facts

The checked-in project uses these versions in `gradle.properties`:

| Component | Version |
| --- | --- |
| Minecraft | 26.1.2 |
| Java | 25 |
| Architectury API | 20.0.7 |
| Fabric API | 0.153.0+26.1.2 |
| NeoForge | 26.1.2.76 |

The baseline command succeeds for all modules:

```text
cmd.exe /c "cd /d A:\Projects\The Experiment Lab\Minecraft\combo && python build-smart.py compile"
```

The following are real, locally resolved API/class entry points for precisely these dependencies. Read these before changing the hook; do not rely on older Minecraft examples.

| Purpose | Verified class/member | Why it matters |
| --- | --- | --- |
| Shared target-click event | `dev.architectury.event.events.common.PlayerEvent.ATTACK_ENTITY` | It is invoked before a player left-click attack and exposes `Player`, `Level`, target `Entity`, hand, and nullable `EntityHitResult`. It is useful for lightweight prechecks, but it does **not** prove that damage landed. |
| Shared lifecycle cleanup | `PlayerEvent.PLAYER_QUIT`, `PlayerEvent.PLAYER_RESPAWN`, `PlayerEvent.PLAYER_CLONE` | Clear transient combo state and prevent stale entries. |
| Shared ticks | `dev.architectury.event.events.common.TickEvent.PLAYER_POST` | Optional HUD timeout sync/cleanup; the core can expire state lazily on the next attack instead. |
| Cross-loader packets | `dev.architectury.networking.NetworkManager` | Supports typed `CustomPacketPayload` registration, receivers, `sendToPlayer`, and `sendToServer`. Only S2C sync is needed for the first version. |
| Actual vanilla melee method | `net.minecraft.world.entity.player.Player#attack(Entity)` | The 26.1.2 mapped bytecode computes attack cooldown, critical/sweep state, and vanilla damage here. Do not replace this method wholesale. |
| Damage application in that method | `Entity#hurtOrSimulate(DamageSource, float)` | `Player.attack` has one direct call on the primary target. Redirecting that exact invoke lets us multiply a finisher hit and only progress the combo when it returns `true`. |
| Extra knockback | `Entity#push(double, double, double)` / `LivingEntity#knockback(double, double, double)` | Apply only after a confirmed finisher. Use the player-to-target horizontal vector and guard zero length. |
| Client HUD lifecycle | `dev.architectury.event.events.client.ClientTickEvent.CLIENT_POST` | Decrement/interpolate client-only display state; render registration remains loader/client code. |

Local artifacts inspected for this plan:

```text
/mnt/c/Users/video/.gradle/caches/modules-2/files-2.1/dev.architectury/architectury/20.0.7/43db4b34da3f52e211a7434fc1bc85f446f48e75/architectury-20.0.7-sources.jar
/mnt/c/Users/video/.gradle/caches/fabric-loom/minecraftMaven/net/minecraft/neoforge-26.1.2.76-minecraft-merged-deobf/26.1.2/neoforge-26.1.2.76-minecraft-merged-deobf-26.1.2.jar
```

For a local implementation, use `javap -c -p` against the mapped jar to re-check the injection target. The relevant current invocation is `Entity.hurtOrSimulate(DamageSource, float)` inside `Player.attack(Entity)`.

## Recommended design

### Gameplay rules (first playable version)

Keep these as named constants in one place, rather than scattering literals:

```text
COMBO_LENGTH = 3
COMBO_TIMEOUT_TICKS = 12
MIN_ATTACK_STRENGTH = 0.90F
FINISHER_DAMAGE_MULTIPLIER = 1.75F
FINISHER_EXTRA_KNOCKBACK = 0.65D
```

Rules:

1. Only server-side player attacks are considered.
2. Ignore attacks when `player.getAttackStrengthScale(0.5F) < MIN_ATTACK_STRENGTH`; do not let click-spam build a combo.
3. On a confirmed damage result, increment the player state if the last confirmed hit was within the timeout; otherwise start at stage 1.
4. When the pending hit would be stage 3, multiply only that vanilla primary-target damage. If the damage call returns `false`, do not consume or advance the combo.
5. After a successful finisher, play feedback, apply the small extra knockback, send an S2C result packet, and reset the state to zero.
6. The default scope is normal primary melee targets. Do not make sweeping secondary targets, projectiles, air swings, or attacks blocked by shields advance the chain unless deliberately adding those mechanics later.

The important semantic choice is **confirmed damage**, not merely a left-click or targeted attack. `PlayerEvent.ATTACK_ENTITY` happens before vanilla damage and therefore cannot distinguish an invulnerable/missed target by itself.

### State model

Use a transient `Map<UUID, ComboState>` in a common `ComboCombat` service. No saved component/attachment is needed: combo progress is intentionally short-lived and should not persist across logouts, deaths, or server restarts.

Suggested record/class fields:

```java
int confirmedStage;       // 0..2: state after the latest successful hit
long lastConfirmedTick;   // ServerLevel#getGameTime()
```

Make the service expose a small, testable API such as:

```text
prepareAttack(ServerPlayer, Entity, long) -> PendingAttack
confirmAttack(ServerPlayer, PendingAttack, boolean didDamage)
clear(Player)
```

`PendingAttack` should contain whether this attack is a finisher and its damage multiplier. This keeps the mixin thin and ensures failure paths cannot leave the map one stage ahead.

## File plan

Create these files:

```text
common/src/main/java/net/vg/combo/combat/ComboCombat.java
common/src/main/java/net/vg/combo/combat/ComboState.java
common/src/main/java/net/vg/combo/network/ComboSyncPayload.java
common/src/main/java/net/vg/combo/mixin/PlayerAttackMixin.java
common/src/main/resources/combo.mixins.json
```

Update these existing files:

```text
common/src/main/java/net/vg/combo/Combo.java
fabric/src/main/java/net/vg/combo/fabric/client/ComboFabricClient.java
fabric/src/main/resources/fabric.mod.json
neoforge/src/main/java/net/vg/combo/neoforge/ComboNeoForge.java
neoforge/src/main/resources/META-INF/neoforge.mods.toml
common/src/main/resources/assets/combo/lang/en_us.json
```

Optional later HUD files, after gameplay is proven:

```text
common/src/main/java/net/vg/combo/client/ComboClientState.java
fabric/src/main/java/net/vg/combo/fabric/client/ComboFabricHud.java
neoforge/src/main/java/net/vg/combo/neoforge/client/ComboNeoForgeHud.java
```

## Implementation sequence

### 1. Establish common initialization

In `Combo.init()`:

- Register the S2C typed payload and receiver through `NetworkManager`.
- Register `PLAYER_QUIT` and `PLAYER_RESPAWN` cleanup.
- Do not register client classes from common init.

The packet should contain only presentation data: stage `0..2`, remaining timeout ticks, and a boolean/enum indicating a finisher. The server owns all gameplay state; the client must never report combo progress.

### 2. Add the shared mixin configuration

Add `common/src/main/resources/combo.mixins.json` with package `net.vg.combo.mixin`, Java 25 compatibility, `defaultRequire: 1`, and `PlayerAttackMixin` under `mixins`.

Declare the same config in both loader metadata files:

- Fabric: add `"mixins": ["combo.mixins.json"]` to `fabric.mod.json`.
- NeoForge: add:

```toml
[[mixins]]
config = "combo.mixins.json"
```

This exact metadata pattern is already used by neighboring 26.1.2 projects, for example `../sleepcycle/common/src/main/resources/sleepcycle.mixins.json`, its Fabric metadata, and its NeoForge TOML.

### 3. Implement the narrow `Player.attack` redirect

Mixin target:

```text
net.minecraft.world.entity.player.Player
method: attack(Lnet/minecraft/world/entity/Entity;)V
invoke: Lnet/minecraft/world/entity/Entity;hurtOrSimulate(Lnet/minecraft/world/damagesource/DamageSource;F)Z
```

Use an instance `@Redirect` handler so it can treat the mixin instance as the attacking `Player`.

Handler algorithm:

1. If `this` is not a `ServerPlayer`, invoke `target.hurtOrSimulate(source, amount)` unchanged. Client prediction must remain vanilla.
2. Call `ComboCombat.prepareAttack(serverPlayer, target, serverLevel.getGameTime())`.
3. Call the original `target.hurtOrSimulate(source, amount * pending.multiplier())` exactly once.
4. Pass its boolean result to `ComboCombat.confirmAttack`.
5. If confirmed, send normal stage sync. If it was a confirmed finisher, emit effects and reset sync.
6. Return the original boolean result.

This preserves vanilla cooldown calculation, enchantment damage, critical-hit logic, sweeping logic, durability, statistics, and post-hit logic. Do **not** overwrite `Player.attack` or manually recreate vanilla damage sources.

Because mixin bytecode targets are version-sensitive, keep `defaultRequire: 1` while developing. If the target moves in a future version, fail loudly and re-inspect the mapped `Player.attack` bytecode rather than silently disabling combat logic.

### 4. Feedback and HUD

Server-side finisher feedback can use vanilla `ServerLevel` sound/particle methods so nearby players see it. Reuse a vanilla crit-like particle/sound initially; add custom assets only when the feel is established.

For the local player HUD:

- On every confirmed stage change, S2C sync `stage` and `lastConfirmedTick`/remaining timeout.
- Hold this in a client-only state object.
- Render three small pips or `COMBO 1/3`, `COMBO 2/3`; use an accent treatment for a finisher.
- Clear locally at the server-provided expiration time; do not use the HUD as gameplay authority.

Use loader-native HUD registration if Architectury does not offer a stable 26.1.2 abstraction. Keep the render code out of `common` and wire it from the existing Fabric client initializer and a NeoForge client event subscriber.

## Test matrix

Run the compact compile after each coherent change:

```text
cmd.exe /c "cd /d A:\Projects\The Experiment Lab\Minecraft\combo && python build-smart.py compile"
```

Then test both Fabric and NeoForge in a dedicated-server/client and integrated-server setup:

| Scenario | Expected result |
| --- | --- |
| Three fully charged hits inside 12 ticks | Stages 1 and 2 sync; third damage is multiplied once and resets the combo. |
| Wait longer than timeout before the next hit | Next confirmed hit begins at stage 1. |
| Click while cooldown is low | No stage advance; no finisher charge. |
| Hit invulnerable/immune entity | No stage advance and no finisher consumption. |
| Kill on stage 2 / finisher | No crash; effects and reset occur exactly once. |
| Shielded or otherwise negated hit | Verify `hurtOrSimulate` result; no advance when it returns false. |
| Sweep attack | Only the primary target controls combo stage/finisher. |
| Logout, respawn, dimension change | Combo is cleared or safely expires; no stale HUD. |
| Multiplayer | One player cannot alter another player's state; observers see finisher world effects. |

## Non-goals for the first pass

- Combo persistence in player data.
- Custom weapons, attributes, datapack configuration, or a config screen.
- Air-swing rhythm detection. That needs a separate client input hook plus a validated C2S packet and should not be conflated with confirmed melee hits.
- Replacing vanilla attack animations or attack cooldown behavior.

Build the confirmed-hit version first. Once it feels good, configuration, weapon/tag restrictions, particles, sounds, and a more elaborate HUD are straightforward additions.
