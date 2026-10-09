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

The separate publishing session must choose the next version and release notes,
complete the manual regressions, and run the established dry-run/preflight process
before any explicitly authorized publication.
