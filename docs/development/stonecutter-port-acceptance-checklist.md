# Stonecutter multi-version port acceptance checklist

Use this checklist when converting another Architectury mod to a Fishing
Frenzy-style Stonecutter matrix. It is a release gate, not merely a list of
Gradle tasks: a target is unsupported until its **installable jar** has passed
the applicable runtime checks.

Read `stonecutter-multiversion-migration.md` first. That document explains how
to build the matrix; this one defines when the work is actually complete.

## 1. Establish the baseline

- [ ] Record the mod's existing Minecraft version, Fabric/NeoForge versions,
  Java version, Architectury version, and known-good launch commands.
- [ ] Start the original target on both loaders before changing build files.
- [ ] Inspect `git status --short`; preserve all user-owned changes.
- [ ] List every direct Minecraft API, mixin target, resource format, and
  loader-native hook likely to differ between the oldest and newest targets.
- [ ] Identify sentinel targets: oldest Fabric, oldest NeoForge, canonical,
  and each known dependency/API boundary.

## 2. Design the matrix before porting code

- [ ] Create one explicit `<mc-version>-fabric` and `<mc-version>-neoforge`
  node per supported pair, with pinned property files.
- [ ] Keep the main `common/`, `fabric/`, and `neoforge/` trees canonical for
  the maintained/current API generation.
- [ ] Give each node an isolated run directory.
- [ ] Select Java 21 for 1.21/1.21.1 and Java 25 for 26.x where those are the
  game's requirements. Configure legacy toolchains at project level before
  Loom creates run tasks; do not combine `javaLauncher` and `executable`.
- [ ] Write a narrow, named generated-source/resource transform only after
  proving a specific incompatibility. Never edit `build/matrix-*` output.

## 3. Handle compatibility differences deliberately

- [ ] Separate common behavior from Fabric and NeoForge bootstrap/client code.
- [ ] Verify every mixin target against the exact mapped game version; omit a
  version-only mixin from targets where its target does not exist.
- [ ] Translate data/resource formats only for affected legacy nodes (for
  example item definitions, recipe ingredient shape, or model predicates).
- [ ] Validate client-only registrations independently on Fabric and NeoForge.
- [ ] For vanilla-derived behavior, inspect the target version's resource JSON,
  concrete implementation class, and actual method body before adapting it.
- [ ] Verify Mixins/invokers against exact runtime JVM descriptors; a lookalike
  local type is not descriptor-compatible with a private target type.
- [ ] If legacy item colors use a color-provider callback, return opaque ARGB
  values (`0xFFRRGGBB`), not 24-bit RGB values.
- [ ] Record every transform's reason next to the transform and in the
  migration guide.

## 4. Build the correct artifact

- [ ] Before the full matrix build, complete compile, package, runtime, and
  representative gameplay/visual validation on the sentinel targets.
- [ ] Expand compatibility transforms only after their sentinel behavior works.

- [ ] Run `python build-smart.py matrix:compile`.
- [ ] Run `python build-smart.py matrix:package`.
- [ ] For 1.21/1.21.1, distribute only the **remapped** `remapJar` output: the
  jar without `-dev` in its name. Never install the Mojang-named `*-dev.jar`.
- [ ] For 26.1+, distribute the normal `jar` output; this game line is
  unobfuscated and does not use legacy remapping.
- [ ] Confirm the generated release jar sits in
  `build/libs/<target>/`, has the expected metadata, and is not a raw/dev jar.
- [ ] Ensure the matrix packaging aggregate calls `remapJar` for legacy targets
  and `jar` for 26.x targets, so a future release cannot accidentally publish
  a development artifact.

## 5. Runtime acceptance gates

For each loader/version API generation being claimed:

- [ ] Start `runClient` and reach a stable title screen after initial resource
  reload.
- [ ] Start `runServer`, reach its ready/`Done` line, then stop it cleanly.
- [ ] Confirm shared initialization never loads a client class on a dedicated
  server.
- [ ] Install the produced release jar in a real external launcher instance
  (for example Prism), with matching Minecraft, loader, Java, Architectury,
  and required dependencies.
- [ ] Confirm that external instance reaches the title screen; this catches
  development-mapping/remapping mistakes that `runClient` cannot.
- [ ] Exercise representative gameplay: registry entries, models/textures,
  recipes/loot, networking, and any feature touched by transforms.
- [ ] Record results in a compact ledger with target, commit, artifact type,
  client, server, external-launcher, and gameplay status.

## 6. Close out safely

- [ ] Update the migration guide with newly discovered differences and their
  exact workaround.
- [ ] State clearly which exact targets had external release-jar tests versus
  development-only smoke tests.
- [ ] Stage only port files; do not stage generated outputs, run worlds,
  caches, or unrelated user edits.
- [ ] Make a coherent commit with the verified scope.

## Definition of done

Do **not** call the port complete because all Java targets compile. It is
complete only when every claimed target has the correct distribution artifact,
the required client/server runtime smoke tests, and documented exceptions.
