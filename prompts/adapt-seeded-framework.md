The reusable Minecraft mod framework has already been seeded into this repository from the current gold-standard template.

Do NOT reconstruct the framework from sibling repositories and do NOT redesign it.

Start with:

    git status --short
    git diff --check

Treat these as established reusable infrastructure:

    AGENTS.md
    build-smart.py
    scripts/smoke-release-client.py
    scripts/publish-release.py
    scripts/test-publishing.py
    gradle/publishing.gradle
    .github/workflows/release.yml
    docs/development/

The following seeded files REQUIRE project-specific adaptation before use:

    scripts/verify-matrix-artifacts.py
    gradle/publishing.properties

Also inspect only the small project-specific assumptions in:

    build-smart.py
    scripts/smoke-release-client.py
    scripts/publish-release.py
    .github/workflows/release.yml

Adapt only:

- mod ID / display name
- Java package/class origin
- actual loader metadata/environment
- production dependency mappings
- Modrinth project ID/slug
- CurseForge project ID/slug
- artifact naming if this repo differs
- verifier required classes/resources/mixins
- any project-specific smoke marker/class origin
- workflow artifact/concurrency labels if still hard-coded
- release version/notes requested by the task

Do NOT overwrite or redesign:

    settings.gradle
    stonecutter.gradle
    build.matrix.gradle
    gradle/matrix/*.properties
    common/
    fabric/
    neoforge/

unless the task specifically requires a real compatibility/build fix.

Before accepting the seed:

1. compare the new framework files against the previously tracked versions;
2. preserve any project-specific behavior that is still needed;
3. remove every leftover reference to the gold-standard source mod;
4. run helper/unit/static checks before expensive matrix work.

Search for leaked template identities before preflight, including:

    lootexplorer
    LootExplorer
    structurevoidable
    StructureVoidable
    sagittary
    Sagittary

References in historical documentation are allowed only when intentionally explanatory. They must not remain in active publication/build configuration.

Once the framework is adapted, follow the project-specific task prompt.

Do not expose `.env` contents.
