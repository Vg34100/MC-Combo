# Validation and Release

## Principle

Use the smallest evidence set that can disprove the current change.

Do not use “more validation” as a substitute for choosing representative compatibility shapes.

## Validation Levels

### 1. Compile

Use during source/API work on the affected sentinel.

### 2. Package

Use when remapping, metadata, resources, or artifact construction matters.

### 3. Artifact Verification

Use `scripts/verify-matrix-artifacts.py` to prove release-JAR structure.

### 4. Development Runtime

Use `runClient` / targeted server checks for actual gameplay/UI/mixin behavior.

### 5. Packaged-JAR Production Smoke

Use the exact installable release JAR in an isolated runtime.

This should prove:

- staged artifact hash
- loaded mod class origin
- deterministic startup/resource milestone
- no accidental alternate mod JAR
- bounded owned-process shutdown

## Default Representative Matrix

```text
26.2 Fabric
26.2 NeoForge
1.21.1 Fabric
1.21.1 NeoForge
```

Add another target only for a genuinely unique boundary.

## Full Matrix Acceptance

After sentinel stability:

```bash
python build-smart.py matrix:compile
python build-smart.py matrix:package
python scripts/verify-matrix-artifacts.py
```

Run once for acceptance.

Do not repeat after no-op/documentation-only work.

## Manual Regression

Derive a short checklist from the mod's actual features.

Do not manually test all 12 clients unless 12 distinct runtime mechanisms truly exist.

## Release Preflight

After release version, publishing metadata, notes, and artifacts are final:

```bash
python build-smart.py publish:preflight
```

Expected composition:

```text
package
→ artifact verification
→ representative packaged release smoke
→ Modrinth dry-run
→ CurseForge dry-run
```

Run the full preflight once.

## Failure Handling

Classify before changing code:

```text
environment/JDK
target selection
Minecraft API
dependency ABI
compile
resource/metadata
mixin/runtime
release artifact
publishing metadata
real upload
```

Do not respond to every failure by rebuilding the whole matrix.

## Stop Rule

Once the requested acceptance evidence is green, stop unless a later edit invalidates it.
