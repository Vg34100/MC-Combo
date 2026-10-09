# Framework Seed Status

The standardized framework has been adapted to Combo and its 12 Stonecutter targets.
Target/dependency facts are owned by `gradle/matrix/*.properties`; loader source remains
under `common/`, `fabric/`, and `neoforge/`.

Compile, package, and installable-artifact gates passed for all 12 targets. Packaged
client smokes passed for the four configured sentinels, and six dedicated-server
startup checks passed. See [the migration acceptance ledger](docs/development/combo-migration.md)
for exact evidence, compatibility boundaries, and outstanding manual gameplay checks.

Publication configuration uses Combo's confirmed Modrinth and CurseForge projects,
both client/server environments, and actual required/optional dependencies. The
published `0.1.0` version is unchanged and duplicate uploads are rejected. No platform
upload, release tag, or GitHub Release was created during migration.

Release `0.1.1` has now been prepared with its version and release notes synchronized.
The full publication preflight passed: 12 packaged/verified artifacts, four
representative installed-client smokes, and 12 official upload payload dry-runs on
each platform. Manual gameplay/visual checks remain documented in the migration
ledger; the release notes make no new gameplay-fix claims.

Real publication uses the established sequential, receipt-backed uploads of those
exact verified JARs. Push the matching release tag only after the external platform
uploads succeed; the tag workflow creates the GitHub Release without republishing
Modrinth or CurseForge.
