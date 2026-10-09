# Publishing

## Goal

One source release, one exact 12-node publication plan, safe platform uploads, and one GitHub Release containing only installable matrix JARs.

## Source of Truth

Use:

```text
gradle/publishing.properties
gradle.properties
gradle/matrix/*.properties
loader metadata
docs/wiki/release-notes.md
```

Credentials are never publication configuration.

## Credentials

Root ignored `.env`:

```dotenv
MODRINTH_TOKEN=...
CURSEFORGE_TOKEN=...
```

Rules:

- never commit `.env`
- never print token values
- never pass tokens as command-line arguments
- plan/dry-run operations should not require real credentials

## Publishing Properties

Project-specific values typically include:

```properties
mod_id=
mod_name=
environment=
modrinth_project_id=
modrinth_project_slug=
curseforge_project_id=
curseforge_project_slug=
version_type=release
version_name=[{loader_name}] {mod_name} {mod_version} ({minecraft_version})
version_number={mod_version}-{minecraft_version}-{loader}
github_tag=v{mod_version}
github_title={mod_name} {mod_version}
notes_file=docs/wiki/release-notes.md
notes_section={mod_version}
```

Add dependency mappings only for dependencies actually declared by loader metadata.

## Semantic Duplicate Rule

A semantic publication node is:

```text
mod version + exact Minecraft version + loader
```

Historical display names/internal version strings do not justify reuploading the same semantic node.

## Plan and Dry Runs

Expected commands:

```bash
python build-smart.py publish:plan
python build-smart.py publish:modrinth-dry-run
python build-smart.py publish:curseforge-dry-run
python build-smart.py publish:all-dry-run
```

Dry-runs must never upload.

## One Preflight

After all publication facts are final:

```bash
python build-smart.py publish:preflight
```

Expected:

```text
package
→ verifier
→ representative release smoke
→ both platform dry-runs
```

If green, do not rerun solely for reassurance.

## Real Uploads

Real publication requires explicit confirmation.

Uploads should be:

- sequential
- receipt-backed
- resume-safe
- exact-artifact-hash checked

On partial failure:

- preserve successful receipts
- do not rollback public files
- do not reupload successful nodes
- stop if the required fix changes source/artifact bytes/version/publication semantics

## GitHub Release

Recommended order:

1. commit/push exact release revision
2. complete external platform publication
3. create annotated `v<mod_version>` tag on that exact revision
4. push tag
5. GitHub Actions rebuilds/verifies and creates one release
6. release contains exactly the installable matrix JARs

The GitHub workflow must not republish Modrinth/CurseForge.

## Changelog Style

Keep user-facing release notes brief.

Do not mention:

- AI
- Stonecutter
- internal migration architecture

Describe user-relevant version/loader support and actual feature changes only.
