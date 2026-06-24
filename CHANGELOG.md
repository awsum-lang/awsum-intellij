# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

`awsum-intellij` is versioned 1:1 with the `awsum` compiler — the extension's `A.B.C` is exactly the `awsum` `A.B.C` it targets. All three IDE plugins (`awsum-vscode`, `awsum-zed`, `awsum-intellij`) handshake the compiler version at LSP startup; collapsing the plugin and compiler version axes into one keeps that handshake trivial — no per-plugin compatibility table to maintain. Every `awsum` release ships a matching extension release, the extension is never released ahead of the compiler, and only the latest `awsum` release is supported.

Until `awsum 1.0.0`, the project does not follow SemVer — every release increments only the patch (`0.0.1 → 0.0.2 …`), and any release may break. The 1:1 lockstep above is the contract that does hold: within a single `0.0.x`, the extension and the `awsum` it ships against are mutually compatible.

## [Unreleased]

### Fixed

- Syntax highlighting no longer extends an identifier across a non-ASCII letter — the grammar's identifier patterns are now ASCII `[A-Za-z0-9_']`, matching the compiler, which rejects non-ASCII identifiers. Previously the TextMate `\w` matched Unicode, so a name like `funcλ` was highlighted as one valid identifier.

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
