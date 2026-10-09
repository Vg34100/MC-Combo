# Multiversion Playbook

## Purpose

Move a working Architectury/Fabric/NeoForge mod into one maintainable Stonecutter matrix without duplicating whole source trees or turning Gradle into a Java source generator.

## Target Matrix

Current suite target set:

```text
1.21-fabric
1.21-neoforge
1.21.1-fabric
1.21.1-neoforge
26.1-fabric
26.1-neoforge
26.1.1-fabric
26.1.1-neoforge
26.1.2-fabric
26.1.2-neoforge
26.2-fabric
26.2-neoforge
```

## Architecture

```text
settings.gradle
stonecutter.gradle
build.matrix.gradle
gradle/matrix/*.properties

common/
fabric/
neoforge/

gradle/compat/             # only when real divergence requires it
gradle/resource-compat.gradle  # only when serialized formats differ

build-smart.py
scripts/verify-matrix-artifacts.py
scripts/smoke-release-client.py
```

Responsibilities:

- `settings.gradle`: register matrix nodes, early loader/platform facts.
- `stonecutter.gradle`: active target + aggregate tasks.
- `build.matrix.gradle`: generic per-target build configuration.
- `gradle/matrix/*.properties`: version/loader/Java/dependency facts.
- source trees: canonical maintained behavior.
- compatibility area: only the small files that genuinely diverge.

## Migration Order

1. Record the current baseline.
2. Make the canonical current target build through the matrix.
3. Prove modern sentinels (`26.2` Fabric/NeoForge).
4. Prove legacy sentinels (`1.21.1` Fabric/NeoForge).
5. Fill adjacent nodes.
6. Run the full compile/package/artifact gate once.
7. Run representative packaged-JAR smoke.
8. Perform short manual feature regression.
9. Stop.

## Baseline

Before architecture changes, prove the existing supported loaders compile/package.

Do not repeatedly rebuild the baseline after recording it unless later evidence suggests a regression.

## Sentinel Strategy

Use compiler/runtime failures from representative shapes to discover boundaries.

Do not attack all 12 nodes at once.

Add another sentinel only if a target introduces a unique compatibility mechanism.

## API Investigation

For each failure:

1. read the smallest useful error
2. identify the affected API
3. query exact target API through Minecraft MCP
4. classify using `compatibility-policy.md`
5. patch the smallest readable boundary
6. rerun only the affected sentinel

## Adjacent Targets

Once major compatibility generations are stable:

- verify adjacent targets narrowly
- do not assume neighbors are identical
- do not launch every client

## Optional Integrations

Keep development fixtures separate from production dependencies.

Target-aware dev integrations may live in a dedicated Gradle helper and matrix pins.

They must not leak into:

- required loader metadata
- packaged release artifacts
- publication dependency relations

## Full Gate

After sentinel stability:

```bash
python build-smart.py matrix:compile
python build-smart.py matrix:package
python scripts/verify-matrix-artifacts.py
```

Artifact verification should reject:

- missing target artifact
- stale/duplicate artifact selection
- dev/source JAR instead of installable JAR
- wrong loader/MC metadata
- wrong Java level
- missing expected classes/resources/mixins
- malformed transformed resources
- bundled optional integrations

## Runtime

Use representative compatibility coverage, not symmetry.

Typical suite coverage:

```text
modern Fabric
modern NeoForge
legacy Fabric
legacy NeoForge
```

Development `runClient` proves behavior; packaged release smoke proves the installable JAR.

## Publishing

Only after build/runtime architecture is stable.

Use `publishing.md`.

## Feature Development After Migration

For normal future features:

1. develop deeply on one current canonical target
2. test behavior there
3. port across the matrix
4. add compatibility only where a real difference appears
5. sentinel-check during port
6. save aggregate/release gates for release preparation
