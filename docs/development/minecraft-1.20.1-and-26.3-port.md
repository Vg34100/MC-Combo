# Combo: Minecraft 1.20.1 and 26.3 port

The matrix now discovers sixteen targets from `gradle/matrix/*.properties`.
The four new pin files are authoritative for their Java and dependency versions.
Existing twelve-target pins, mod version, stat/packet/key identifiers, configuration
backend, filename policy, keys/defaults, and saved data are unchanged.

## Compatibility boundaries

- `ComboIds.id` contains the 1.20.1 identifier-constructor boundary. No item-property
  factory was added: Combo does not register items that need one.
- Architectury 9 uses raw channels on 1.20.1. The same six packet paths and primitive
  layouts are retained. Decode occurs before leaving the buffer callback; game work
  remains queued. Later versions keep their typed codecs. Common C2S/server S2C
  registration is separated from physical-client receivers. The existing server
  validation of dodge input, cost/cooldown, yaw rotation and motion sync remains.
- Forge 47 has its own bootstrap and client subscribers. Its event bus is registered
  before shared registries; the dodge key uses `RegisterKeyMappingsEvent`. Config
  screens, HUD overlays and legacy world text stay in physical-client hooks.
- 1.20.1 reuses the existing option helpers with the old `OptionsSubScreen` import,
  list/init/render lifecycle, and the HUD's float partial tick. Config storage and
  modern screens are retained. Sword/axe/other mechanics remain; 1.20.1 has no
  vanilla mace, so the unavailable mace class/sound are guarded rather than backported.
- 26.3 adds shield-callback and visibility-method arguments, and renames the same
  persistent invulnerability flag to `setPermanentlyInvulnerable`. Local guards
  preserve parry, stealth, dodge timing and the `Invulnerable` saved key.
- Three-hit chains, strength capture before vanilla's reset, weapon feedback,
  parry/counter-finishers, stamina and sneak logic retain their existing algorithms
  and tuning. Full Sneak Mode still requires actual sneaking. Takedowns still pass
  through ordinary damage; no direct kill or new combat subsystem was introduced.

## Infrastructure

Forge metadata, mixin manifest, Java 17 bytecode and pack metadata are verified.
The existing verifier also recognizes 26.3's NeoForge icon field. Setup, production
launchers, publication discovery/labels and the tag workflow recognize Forge/Java 17.
The workflow still does not publish to external mod platforms. Twenty Zed tasks
provide sixteen clients plus four existing utilities. Dependency pins come from
Fishing Frenzy/the completed template; no blanket runtime compatibility patch was copied.

Production smoke reuses the reference's hash-pinned PortableMC implementation on
Windows/Linux. Local Linux validation reused cached Java 17/21/25 toolchains,
assets and an isolated newer libc for that launcher. These host paths are not
committed. Optional Mod Menu remains optional and unbundled.

## Acceptance

| New target | Java | Compile/package/artifact | Runtime evidence |
| --- | --- | --- | --- |
| 1.20.1 Fabric | 17 | Pass | Packaged client; development dedicated server |
| 1.20.1 Forge | 17 | Pass | Packaged client and production dedicated server |
| 26.3 Fabric | 25 | Pass | Packaged client-state check; client/server startup not launched |
| 26.3 NeoForge | 25 | Pass | Packaged client and production dedicated server |

The final aggregate `matrix:compile`, `matrix:package` and artifact-verification
sequence passed **16/16**. Compile/package ran once after runtime fixes stabilized;
no prior target runtime was repeated. New-component/Forge-pack checks also passed.
The smoke-tested hashes still match the final matrix outputs.

Three packaged clients prove the exact staged SHA-256, loaded-class JAR origin,
completed initial resource reload/first screen, and termination of the owned JVM.
The two production servers contain the installable Combo JAR and Architectury;
both reach `Done`, save and exit cleanly with matching loaded origins/hashes.
The Fabric server check uses development classes and proves its separate physical-side
registration boundary; it is not additional packaged-server evidence.

Six packet fixtures passed exact IDs, wire bytes and full decode round trips on
both 1.20.1 loaders. Packaged client-state checks passed on all four additions
(combo expiry, finisher flash, next-chain replacement and stamina synchronization).
Publication safety: 19 tests passed. Setup safety: 21 tests passed.

## Manual checks still required

Use matching client/server JARs and ordinary survival players on 1.20.1 and 26.3:

1. Land three sufficiently charged sword/axe hits: pips advance, the finisher fires,
   another chain starts, and waiting beyond weapon cooldown plus configured slack
   expires the chain. Check weapon-specific feedback; check mace splash on 26.3.
2. Raise a shield within the configured parry window: attacker staggers, stars appear,
   stamina spikes and the next counter-hit finishes. Compare a late ordinary block.
3. Sprint/block/sneak to drain stamina; verify delayed regeneration, exhaustion and
   refusal of unaffordable dodges. Press V with W/A/S/D, diagonal and no input at
   different yaw angles; verify direction, cost, cooldown and brief damage immunity.
4. Actually sneak behind an unaware eligible mob; stand up, disable the option, or
   exhaust stamina to leave Full Sneak Mode. Check backstab/takedown range and that
   damage immunity/armor and boss exclusions still apply through ordinary damage.
5. With two clients, compare motion and stamina with the server, attacker-only pips/
   damage feedback, nearby stagger stars, parries, and clearing state on death/rejoin.
6. Inspect HUD/popups/vignette and the settings screen, including 26.3 Fabric rendering.
   Save/reopen existing config files and check Mod Menu present/absent behavior.

Startup and codec checks do not claim live combat or multiplayer packet exchange.
Windows gameplay and the 26.3 Fabric runtime were not repeated. No commit, push, tag, merge or publication
is part of this port; local 0.1.1 outputs do not replace published release files.
