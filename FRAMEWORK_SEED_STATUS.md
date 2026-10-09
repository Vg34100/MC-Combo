# Framework Seed Status

This repository has been seeded from the standardized Minecraft mod framework.

Before build/publish acceptance, adapt and verify the project-specific facts in:

- `scripts/verify-matrix-artifacts.py`
- `gradle/publishing.properties`
- `build-smart.py`
- `scripts/smoke-release-client.py`
- `scripts/publish-release.py`
- `.github/workflows/release.yml`

Use `prompts/adapt-seeded-framework.md`.

Do not publish until all source-mod identity references and project IDs/dependencies/environment/artifact assertions are correct.
