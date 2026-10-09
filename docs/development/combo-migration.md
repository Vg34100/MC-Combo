# Combo multiversion migration acceptance

Accepted build/runtime evidence: 2026-10-09.
Branch: `feat/stonecutter-multiversion`.
Preserved baseline: `8f6c89b15c6c98aef63eff1cf3c9b3fdc83e24d8`.

## Scope and preserved behavior

Combo now uses the established suite Stonecutter controller and central build,
seeded by `Minecraft-Mod-Template` and adapted from the completed sibling mods.
Registered targets are discovered from `gradle/matrix/*.properties`: Minecraft
1.21, 1.21.1, 26.1, 26.1.1, 26.1.2, and 26.2, each on Fabric and NeoForge.
Java 21 applies to 1.21.x; Java 25 applies to 26.x. No machine-specific Java home
is committed. The active editing target remains `26.1.2-fabric`.

The baseline commit safeguards the pre-existing documentation and framework work.
`AGENTS.md`, `docs/GAME_SPEC.md`, configuration implementation/defaults, icon, and
English translations remain byte-for-byte identical to that baseline. Canonical
combat, stamina, stealth, dodge, statistics, networking, and UI implementations
remain under the existing common/loader source roots. No V2 roadmap mechanics were
added. Combo timeout retains the proven weapon-cooldown-plus-configured-slack
calculation; the GAME_SPEC's 12-tick shorthand was not used to retune gameplay.

## Compatibility boundaries

Minecraft drift is handled with local Stonecutter conditions and three narrow
name replacements (identifier, GUI extraction, and slowness effect). Conditions
cover attack/block signatures, knockback arguments, config-screen APIs, and the
legacy shield delay. The original four combat mixins remain active in every target.

Legacy on-foot dodge sends the actual client movement input because those server
versions do not synchronize it for walking players. The server checks finite,
bounded input and rotates it using server yaw. Existing stamina cost, cooldown,
idle backward dodge, velocity synchronization, and invulnerability lifecycle
remain unchanged. Legacy dedicated servers register S2C payload types without
loading client receiver handlers.

The only separate legacy rendering helper draws the existing shared damage-popup
and orbit-star state through the old world-render API. Its text scale, orientation,
alignment, fading, positions, and full-bright display follow the modern baseline.
Fabric and NeoForge retain their native HUD/world callbacks. NeoForge client setup
and config-screen registration now live in a client-only entrypoint, so common
bootstrap does not load client screen classes on dedicated servers.

Combo has no item/recipe/data schema requiring a resource conversion. Metadata
uses exact per-target Minecraft/loader/Java pins, the real icon, all combat mixins,
and both client/server support. Mod Menu remains an optional Fabric integration.
The 16 tracked Zed tasks provide the 12 clients plus four suite utilities, using
native Windows `python` and `$ZED_WORKTREE_ROOT`.

## Acceptance results

Commands use the established native Windows environment and smart wrapper.

| Gate | Result | Evidence scope |
| --- | --- | --- |
| Original 26.1.2 build | 2/2 pass | Fabric and NeoForge baseline compile/package before conversion |
| `python build-smart.py matrix:compile` | 12/12 pass | All registered versions/loaders |
| `python build-smart.py matrix:package` | 12/12 pass | Installable release JARs |
| `python scripts/verify-matrix-artifacts.py` | 12/12 pass | Metadata, exact dependency ranges, class major versions, entrypoints, all mixins, icon/resources, remapping, no bundled external mods |
| `python scripts/test-client-state.py` | 4/4 pass | Real packaged HUD/stamina classes: idle/confirmed-hit pips, timeout refresh/expiry, finisher flash, next-chain replacement, stamina synchronization |
| Configured packaged-client smoke | 4/4 pass | Exact installed release JAR, SHA-256, class origin, startup/resource reload marker, owned JVM termination |
| Dedicated-server startup | 6/6 pass | 1.21.1, 26.1.2, and 26.2 on both loaders; reached `Done` and stopped cleanly |
| `python scripts/test-publishing.py` | 19 tests pass | Publication planning, duplicate guards, receipts/resume and safety checks |
| `python scripts/test-setup.py` | 21 tests pass | Native environment/bootstrap support |
| Zed task inspection | 16/16 pass | Matrix coverage, native launcher, worktree cwd |
| Live read-only duplicate check | Pass | Rejects both already-published 0.1.0 / 26.1.2 Modrinth uploads |

After the final legacy text alignment correction, all four legacy targets were
recompiled, the matrix was repackaged and verified, packaged state checks passed,
and both legacy client smokes were repeated. Modern artifacts were unchanged.
Server evidence remains valid because this correction affects client rendering only.

### Final packaged-client evidence

Every row verifies the current installable artifact rather than a development JAR.
The loaded Combo class originates from that exact staged artifact. Player/living
entity classes (and modern BlocksAttacks) loaded without required-mixin failures.
All four checks recorded termination of their own JVM.

| Target | Release JAR SHA-256 | Startup / initial resource reload |
| --- | --- | --- |
| 26.2 Fabric | `6f3fa08755644ce3f4cb4619970dffc200fa9b16a54cc4a662e29cada307a8b4` | `startedCleanly:true` |
| 26.2 NeoForge | `1cf6827324d534c5927a0f21bb2d679ed0e3e17053bb35bff6698c032922dd0f` | `startedCleanly:true` |
| 1.21.1 Fabric | `0847e789fcb2305c3905a2bfb7eb48efbdf6f160922d8cceb7fb32c4e648d3ab` | Initial reload + first-screen initialization, stable for 3 seconds |
| 1.21.1 NeoForge | `81b334441a0166ab700e984898bcdd3f15be0861f9d1aedc194a2ba98fdbb324` | Initial reload + first-screen initialization, stable for 3 seconds |

Local smoke receipts remain ignored under `%TEMP%\combo-release-smoke\<target>\<run>\passed.txt`.
Run tokens, in the order above: `f44ddbd2c97144c592fffecaf14eeab4`,
`79db1040cd204463a1a9a70ccd0866e7`, `a50b2037a27b4fb68f37d67e9c18fcdf`,
and `75157fec3b6a42a68c8103448d565618`. Build output, run worlds, raw logs, downloaded
tools, and receipts are not committed.

## Remaining manual gameplay checks

Startup and packaged state checks do not exercise live combat, rendered appearance,
or multiplayer timing. Before publication, use installed JARs on representative
1.21.1/26.2 Fabric and NeoForge clients, including matching dedicated servers:

- Confirm charged melee hits advance the chain, failed/weak hits do not, idle
  timeout resets it, and the third hit triggers the existing weapon-specific
  finisher. Check sword bleed, axe/mace knockback, and mace area damage.
- Compare reactive parry with sustained blocking; check counter-finisher,
  stagger duration, shield delay, stamina costs, and cooldowns.
- Check sneaking detection reduction, rear blind spot versus omnidirectional
  detection, backstab/takedown conditions, and excluded targets.
- Dodge forward/back/left/right, diagonally, and from idle at different yaw angles.
  Check distance, synchronized motion, stamina/cooldown rejection, invulnerability
  expiry, and cleanup after death/respawn.
- Check sprint/block/sustained-sneak stamina drain, exhaustion and regeneration.
  Inspect pips, finisher flash, stamina auto-hide, damage text, orbit stars, and
  vignette appearance across GUI scales.
- Open, change, save, and reload configuration through Fabric Mod Menu and
  NeoForge's native screen. Check Fabric startup without Mod Menu as well.
- Verify custom statistics and death/logout/respawn cleanup; test multiplayer
  combat feedback and dodge synchronization, then perform an in-world F3+T reload.

These checks are pending human validation. No claim of complete in-world gameplay
or visual regression acceptance is made by the automated gates.

## Publication readiness and deferred release stage

Public project facts are confirmed:

- [Modrinth Combos](https://modrinth.com/mod/combos): project `7X8HTWbD`, slug
  `combos`; public API confirms required client and server installation.
- [CurseForge Combos](https://www.curseforge.com/minecraft/mc-mods/combos): project
  `1732433`, slug `combos`, supplied and confirmed by the project owner.

`gradle/publishing.properties` declares Architectury as required on both loaders,
Fabric API as required on Fabric, and Mod Menu as optional on Fabric. No unrelated
JEI dependency or test integration is published. Combo identity, artifact checks,
both environments, release-note path, safety tests, and workflow labels are adapted.
The GitHub workflow does not upload to Modrinth or CurseForge.

`mod_version` remains `0.1.0`; no next version was selected. Existing Modrinth
versions `zS2XIjcV` (Fabric) and `fcEPhQBV` (NeoForge) remain untouched. Publication
planning is read-only, and the live semantic duplicate guard rejects those existing
uploads. No platform upload, release tag, or GitHub Release was performed.

The separate publishing session must choose the next version, add its final
release-note section, complete the manual checks above, then follow `publish:plan`,
platform dry-runs, and one `publish:preflight`. Commit/push the exact release revision
before explicitly authorized sequential uploads, tagging, and GitHub Release.
Credentials remain in ignored `.env`/environment only. Existing successful uploads
must be preserved on any resume.
