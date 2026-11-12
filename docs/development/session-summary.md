# Combo 16-target compatibility port — 2026-10-18

Baseline: `main`, `318198b9d23bdb7700d90d74ba20154f70a5180a`; initially clean.
All port changes remain unstaged and uncommitted. No push, tag, merge or publication
was performed; `mod_version=0.1.1` and public releases are unchanged.

- Added 1.20.1 Fabric/Forge (Java 17) and 26.3 Fabric/NeoForge (Java 25).
- Reused the completed template/Fishing Frenzy's pins and native loader patterns.
  Added the identifier factory, legacy channel adapter, physical-client receivers,
  Forge entrypoints/input/render hooks, and narrow API/GUI/mixin guards.
- Existing combat algorithms, configuration backend/defaults, identifiers and
  twelve original dependency pins remain. No combat redesign or rebalancing.
- Final aggregate compile/package/artifact gate: 16/16 passed once after stable
  source. Final JARs retain the representative runtime hashes.
- Three packaged clients passed (1.20.1 Fabric/Forge, 26.3 NeoForge); two production
  servers passed (Forge/NeoForge). The legacy Fabric development server also passed.
- Six exact packet fixtures passed on both 1.20.1 loaders; four packaged client-state
  checks passed. Publishing safety: 19 tests; setup safety: 21 tests passed.
- Actual combat, multiplayer exchange and GUI/visual sign-off remain manual;
  26.3 Fabric startup and Windows gameplay were not repeated. The existing default
  four-target release-smoke selection was not expanded to eight clients.

See [compatibility boundaries and exact manual checks](minecraft-1.20.1-and-26.3-port.md).
Ignored local evidence is under `build/port-validation/`; raw transient test logs
are under `/tmp/combo-port/`. Future release preparation needs separate approval.
