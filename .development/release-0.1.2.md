# Combo 0.1.2 release

The owner authorized publishing the completed sixteen-target port to Modrinth,
CurseForge and GitHub, using exactly the 0.1.2 section in
`docs/wiki/release-notes.md`. Existing releases remain unchanged.

The owner tested the compiled JARs in PrismMC on 1.20.1 Forge and 26.3 Fabric
and accepted the current state for this initial release. Detailed feature and
multiplayer testing remains follow-up work; no rebalance is part of this release.

Three zero-filled source files were restored from the committed baseline and the
existing port transformations. All 48 affected class files across the sixteen
rebuilt JARs match the previously tested 0.1.1 port artifacts byte for byte.

The sixteen 0.1.2 JARs are frozen in the ignored preflight manifest. A host launcher
failure interrupted the first preflight after packaging, artifact verification
and 26.2 Fabric startup passed. Only unfinished checks are resumed; successful
checks and JARs are retained. Durable results and upload receipts live under
ignored `build/publishing/`.

Develop and commit on `work`. The release tag must reference its validated public
snapshot on generated `main`; record the work and public revisions in the local
release report. GitHub's existing workflow builds/verifies its matrix assets and
does not republish either mod platform.

The resumed preflight completed: all four configured packaged clients and both
sixteen-target platform dry-runs passed. A summary-print encoding failure occurred
after the successful dry-run process; no successful check was repeated.
