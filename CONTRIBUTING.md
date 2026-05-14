# Contributing to `awsum-intellij`

Thanks for your interest in contributing.

## Development setup

See [README.md](README.md) for an overview. Quick reference:

```bash
./gradlew buildPlugin    # Build the plugin distribution (build/distributions/awsum-intellij-A.B.C.zip)
./gradlew runIde         # Launch a sandbox IDE with the plugin loaded
./gradlew verifyPlugin   # Run IntelliJ Plugin Verifier against the target IDE versions
```

The build uses `gradle/wrapper/gradle-wrapper.jar` to download the right Gradle version automatically — you don't need a system Gradle. JDK 21 is required; `settings.gradle.kts` declares `foojay-resolver-convention` so Gradle will auto-download Adoptium Temurin 21 on first build if you don't have a local JDK 21 (Gradle stores it under `~/.gradle/jdks/`).

The extension shells out to the `awsum` CLI for every editor feature (diagnostics, formatting, code actions, document and workspace symbols) — install it from the [compiler repo](https://github.com/awsum-lang/awsum) and make sure `awsum` is on your `PATH` before testing changes that touch those features.

## Signed commits

The `main` branch requires signed commits — every commit you push to a PR needs a verified signature, otherwise the merge button stays grey.

Minimal `~/.gitconfig` for SSH signing:

```ini
[user]
	email = ...
	name = ...
	signingkey = ~/.ssh/id_ed25519.pub
[commit]
	gpgsign = true
[gpg]
	format = ssh
```

For GPG signing instead, set `gpg.format = openpgp` (or omit — that's the default) and point `signingkey` at your GPG key ID. The option name `gpgsign` is git's historical name for "sign this thing" and applies regardless of format.

The same key file must be added to GitHub Settings → SSH and GPG keys as a **Signing Key** (a separate category from Authentication Key, even if you reuse the same file). Verify locally:

```bash
git commit -S -m "test" --allow-empty
git log --show-signature -1
```

If you already made unsigned commits on a feature branch, retroactively sign with:

```bash
git rebase --exec 'git commit --amend --no-edit -S' <range>
```

then force-push your branch.

## Pull requests

- Open against `main`. CI (`ci.yml`) must be green before merge.
- For user-visible changes, add a bullet under `## [Unreleased]` in [CHANGELOG.md](CHANGELOG.md). Infrastructure-only changes (CI, dev tooling, internal refactors) still get an entry so the next release notes are complete.
- Versions are 1:1 with the `awsum` compiler. Bumping the version touches `gradle.properties:pluginVersion`; the `intellij-platform-gradle-plugin` injects that into `plugin.xml` at build time via `patchPluginXml`, so no separate version field needs editing.
