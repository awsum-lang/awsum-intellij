# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

`awsum-intellij` is versioned 1:1 with the `awsum` compiler — the extension's `A.B.C` is exactly the `awsum` `A.B.C` it targets. All five editor plugins (`awsum-vscode`, `awsum-zed`, `awsum-intellij`, `awsum-nvim`, `awsum-emacs`) handshake the compiler version at LSP startup; collapsing the plugin and compiler version axes into one keeps that handshake trivial — no per-plugin compatibility table to maintain. Every `awsum` release ships a matching extension release, the extension is never released ahead of the compiler, and only the latest `awsum` release is supported.

Until `awsum 1.0.0`, the project does not follow SemVer — every release increments only the patch (`0.0.1 → 0.0.2 …`), and any release may break. The 1:1 lockstep above is the contract that does hold: within a single `0.0.x`, the extension and the `awsum` it ships against are mutually compatible.

## [Unreleased]

### Changed

- `Restart Awsum LSP server` now restarts the running server from its own descriptor, as the Language Services widget's Restart button does since IntelliJ IDEA 2026.3, so the server comes back even when no `.aww` tab is open. Previously it stopped the server and started a new one only if a `.aww` file was open. The action stays where the widget's button can't be: in Find Action, bindable to a keyboard shortcut.

### Fixed

- The plugin now declares the two dependencies the IntelliJ Platform LSP documentation requires, `com.intellij.modules.lsp` and `com.intellij.modules.ultimate`. Without them it counted as compatible with IDEs that have no LSP API, such as Android Studio and IntelliJ IDEA open-source builds, where it would install with syntax highlighting only.

## [0.0.8] - 2026-11-08

### Changed

- Requires IntelliJ IDEA 2026.2 or newer (`pluginSinceBuild = 262`; previously 2026.1). The plugin now uses the LSP API names 2026.2 introduced — `LspIntegrationProvider`, `ProjectWideLspClientDescriptor`, `LspClientManager.stopAndRestartClientsIfNeeded` — in place of the `*Server*` names 2026.2 deprecates, so the IntelliJ Plugin Verifier reports no deprecated-API usages. Behaviour is unchanged; the source files are renamed to match (`AwsumLspIntegrationProvider.kt`, `AwsumLspClientDescriptor.kt`).
- Build: IntelliJ Platform Gradle plugin 2.16.0 → 2.19.0 (2.17.0 carries the Kotlin / Java version mappings for the 262 platform), Kotlin 2.4.0 → 2.4.21, Gradle 9.5.0 → 9.8.2.
- Builds against IntelliJ IDEA 2026.2.4 (previously 2026.2.2), the newest 2026.2 patch at least 7 days old; 2026.3 (released 2026-11-05) is still inside the 7-day window, so the minimum stays 2026.2. `verifyPlugin` now checks both the minimum and the newest IDE release.
- CI: every GitHub Action pinned to the commit SHA of a release at least 7 days old (`actions/setup-java` v5 → v6); the JDK is pinned to Temurin 21.0.13.
- The README no longer claims format on save out of the box: IntelliJ reformats on save only once Settings → Tools → Actions on Save → Reformat code is on, and the README now says so.
- The README lists hover (type and doc comment of the name under the cursor). `awsum lsp` has served it since 0.0.5; only the documentation was missing it.

### Fixed

- Syntax highlighting no longer extends an identifier across a non-ASCII letter — the grammar's identifier patterns are now ASCII `[A-Za-z0-9_']`, matching the compiler, which rejects non-ASCII identifiers. Previously the TextMate `\w` matched Unicode, so a name like `funcλ` was highlighted as one valid identifier.
- Block comments are now highlighted in `import` lines, type signatures, `type` declarations, and function parameters. In those positions the grammar didn't recognise `{- … -}` at all, so in `pick : Pick {- note -} -> Int32` the comment's words were coloured as types.

## [0.0.7] - 2026-06-22

### Fixed

- Syntax highlighting no longer mis-tokenises an identifier containing an apostrophe (`'`): a name like `in'` was highlighted as the `in` keyword because the grammar's word boundaries didn't count `'` as part of an identifier. They now do.

## [0.0.6] - 2026-06-03

No user-facing changes to the plugin itself; this release tracks the compiler version bump. Every 0.0.6 language change is in the compiler and surfaces through `awsum lsp`.

## [0.0.5] - 2026-05-31

### Added

- `Restart Awsum LSP server` action (id `org.awsumlang.awsum.RestartLspServer`) — calls `LspServerManager.stopAndRestartIfNeeded(AwsumLspServerDescriptor::class.java)` to stop the `awsum lsp` process and start a new one with the same `initializationOptions`. Useful after a local `stack install` of a new `awsum` build, or to clear any in-memory state on the server. Invoke via Find Action (`Cmd+Shift+A` / `Ctrl+Shift+A`); no default keybinding.

## [0.0.4] - 2026-05-13

### Added

- Initial release. Thin LSP client to `awsum lsp --stdio` (subcommand of the `awsum` compiler binary). Every editor feature is computed inside the compiler and pushed over LSP:
  - **Syntax highlighting** via the shared TextMate grammar (mirrored from [`awsum-lang/awsum-vscode`](https://github.com/awsum-lang/awsum-vscode); IntelliJ-bundled TextMate plugin consumes the same VSCode-style bundle layout).
  - **Format on save** via `textDocument/formatting` — same algorithm as `awsum format`. `LspFormattingSupport.shouldFormatThisFileExclusivelyByServer` is overridden to delegate `.aww` files exclusively to the server even when IDEA's default TextMate-tier formatter would otherwise no-op.
  - **Inline diagnostics** via `textDocument/publishDiagnostics` (debounced 500 ms server-side; `error` / `warning` severity honoured by the IDEA Problems panel).
  - **Quick fixes** via `textDocument/codeAction` — compiler-supplied fixes only; the extension does no language-aware reasoning.
  - **Document outline / breadcrumbs** via `textDocument/documentSymbol`.
  - **Workspace symbol search** (`Cmd+O` / `Ctrl+Shift+N`) via `workspace/symbol`.
- Declarative lockstep version check: the extension passes `initializationOptions: { expectedAwsumVersion, preferButtonsOverLinks: true }` and the server warns on mismatch via `window/showMessageRequest` with an action button that opens the install page through `window/showDocument`. The expected version is baked into the plugin at build time — a Gradle task generates `BuildConfig.kt` from `gradle.properties:pluginVersion` and adds it to the Kotlin source set, so the same value `patchPluginXml` writes into `plugin.xml` is also embedded in the compiled class file (no runtime plugin-descriptor lookup, no dependency on internal IntelliJ Platform APIs).
- Release workflow: pushing a `v*` tag builds the plugin distribution `.zip` and publishes a GitHub Release with it attached. Tag and `gradle.properties:pluginVersion` must match, or the run fails before the build.
- Build provenance via `actions/attest-build-provenance@v4` on the published `.zip` — each release asset gets a Sigstore-signed attestation tying it to the release workflow run and the tagged commit. Users verify with `gh attestation verify awsum-intellij-X.Y.Z.zip --repo awsum-lang/awsum-intellij`.
- Gradle Wrapper validation via `gradle/actions/wrapper-validation@v3` on every CI run — protects against supply-chain tampering of `gradle/wrapper/gradle-wrapper.jar`.
- `CONTRIBUTING.md` — covers the dev-loop commands, the signed-commits requirement on `main`, and the PR / CHANGELOG conventions.
- `justfile` with a single user-facing `just release` recipe — checks out `main`, pulls, reads the version from `gradle.properties`, asks the operator to type the version back as confirmation, then creates an annotated tag and pushes it. Mirrors the same recipe in `awsum/justfile`, `awsum-vscode/justfile`, and `awsum-zed/justfile`.
