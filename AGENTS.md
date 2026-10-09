# AGENTS.md

This file defines the default workflow for Minecraft mod work in this repository.

Detailed reusable procedures live under `docs/development/`.

## Priorities

1. Preserve existing mod behavior unless the task explicitly changes it.
2. Treat the current proven implementation as the behavioral baseline.
3. Keep one source of truth for target/version/dependency/publication facts.
4. Represent version drift with the smallest readable compatibility mechanism.
5. Treat Minecraft-version drift and loader drift as separate axes.
6. Reuse the established build/release framework instead of redesigning it.
7. Preserve unrelated user work.
8. Minimize context and validation cost.
9. Stop once the requested acceptance evidence exists.

## Standard Matrix

The current suite matrix is:

```text
1.21      Fabric / NeoForge
1.21.1    Fabric / NeoForge
26.1      Fabric / NeoForge
26.1.1    Fabric / NeoForge
26.1.2    Fabric / NeoForge
26.2      Fabric / NeoForge
```

When this matrix exists, discover registered targets from:

```text
gradle/matrix/*.properties
```

Do not maintain duplicate hard-coded target lists when matrix discovery is available.

## Repository Ownership

Project-specific source/build facts remain owned by:

```text
settings.gradle
stonecutter.gradle
build.matrix.gradle
gradle/matrix/*.properties
common/
fabric/
neoforge/
```

The reusable framework includes:

```text
build-smart.py
scripts/smoke-release-client.py
scripts/verify-matrix-artifacts.py
scripts/publish-release.py
scripts/test-publishing.py
gradle/publishing.gradle
gradle/publishing.properties
.github/workflows/release.yml
docs/development/
```

Framework files may be seeded from the suite template, but must be adapted to the current project's IDs, dependencies, environment, artifact names, classes/resources, and public platform projects.

## Context Discipline

Search first, read second, edit last.

- Begin with `git status --short`.
- Read only files relevant to the active task/failure.
- Do not dump whole source trees.
- Prefer targeted diffs and excerpts.
- Treat `build-smart.py` as established infrastructure once seeded; do not read/rewrite it wholesale unless it fails or chooses an incorrect plan.
- Use sentinel targets before aggregate matrix gates.
- Do not rerun successful acceptance gates for reassurance.

Task-relevant docs:

- adding/migrating versions → `docs/development/multiversion-playbook.md`
- compatibility representation → `docs/development/compatibility-policy.md`
- validation decisions → `docs/development/validation-and-release.md`
- publishing → `docs/development/publishing.md`

## Minecraft/API Investigation

For Minecraft API changes, mappings, class/method availability, mixin targets, or cross-version questions:

1. `minecraft-dev` MCP
2. current project source/history
3. dependency source/metadata
4. Gradle/JAR inspection
5. `javap` only when narrower methods are insufficient

Do not start with broad cache archaeology.

## Compatibility Hierarchy

Use the smallest mechanism that fits:

1. unchanged shared source
2. local Stonecutter `//?` condition
3. narrow deterministic mechanical replacement
4. parsed resource/data transform
5. separate compatibility implementation
6. small compatibility subsystem only when a whole subsystem truly diverges

Avoid full source copies, giant overlays, broad regex rewriting, and Gradle-generated application source.

## Loader Rule

Keep Fabric-only behavior under Fabric and NeoForge-only behavior under NeoForge when practical.

Do not force meaningful loader differences through an abstraction that is harder to understand than two small native implementations.

## Optional Integrations

Optional integrations must remain optional.

- Do not accidentally add them as required publication dependencies.
- Development runtimes may attach compatible test integrations.
- Test present/absent states when integration behavior changes.
- Do not bundle external mods/datapacks into release artifacts unless explicitly intended.

## Validation Strategy

Use the smallest validation that can disprove the current change.

Default representative compatibility shapes:

```text
26.2 Fabric
26.2 NeoForge
1.21.1 Fabric
1.21.1 NeoForge
```

Add another sentinel only for a genuinely unique boundary.

After sentinels are stable, the normal aggregate gate is:

```bash
python build-smart.py matrix:compile
python build-smart.py matrix:package
python scripts/verify-matrix-artifacts.py
```

Run aggregate gates once per acceptance state, not after every edit.

## Packaged Release Smoke

Development `runClient` does not prove the packaged release JAR.

Representative release smoke should verify:

- exact staged installable JAR
- SHA-256/origin proof
- deterministic startup marker
- resource reload
- owned-process termination

Default suite command:

```bash
python build-smart.py release-smoke
```

Use the configured sentinel set rather than launching every target.

## Publishing

Publishing is a separate acceptance stage.

Expected flow:

```text
publish:plan
→ platform dry-runs
→ ONE publish:preflight
→ commit/push exact release revision
→ real external platform publication
→ tag exact revision
→ GitHub Release
```

Rules:

- credentials only from ignored `.env` / environment
- never print token values
- semantic duplicate guards
- sequential uploads
- durable receipts
- safe resume
- never rollback/reupload successful targets merely for naming consistency
- GitHub workflow must not republish external platforms

## Stopping Rule

Once requested evidence is green, stop.

Continue only when:

- a required criterion remains unresolved
- a later edit invalidates prior evidence
- a deterministic new failure appears
- the user explicitly requests more validation

## Git Safety

Before editing:

```bash
git status --short
```

Never revert unrelated changes.

Never stage:

- `.env`
- build output
- run directories/worlds
- downloaded tools
- caches
- validation logs/receipts unless explicitly intended
- unrelated docs/scripts

Do not commit, push, tag, publish, merge, or delete branches without explicit authorization.

## Closeout

Report only:

- what changed
- compatibility/publication boundaries
- acceptance results
- remaining manual checks
- deferred items
- final `git status --short`

Keep raw build logs and chronological diaries out of the closeout.
