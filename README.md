# Awsum language support for IntelliJ IDEA

IntelliJ Platform plugin for the [Awsum](https://awsum-lang.org) programming language (`.aww` files). A thin LSP client to the `awsum lsp` server bundled with the Awsum compiler — every diagnostic, code action, format edit, document symbol, and workspace symbol is computed inside the `awsum` compiler binary and pushed over LSP.

Works in IntelliJ IDEA (free tier) and every Ultimate-tier JetBrains IDE (Rider, CLion, WebStorm, PyCharm Pro, GoLand, …). The native LSP API has been open to all users since IDEA 2025.2, and Community / Ultimate were merged into one product in 2025.3.

## Features

- **Syntax highlighting** — TextMate grammar shared with [`awsum-vscode`](https://github.com/awsum-lang/awsum-vscode).
- **Format on save / reformat code** — `textDocument/formatting`, same algorithm as `awsum format`.
- **Inline diagnostics** — `textDocument/publishDiagnostics`, pushed on open / save / change (debounced 500 ms server-side). `error` vs `warning` severity honoured by the IDEA Problems panel.
- **Quick fixes (intention actions)** — `textDocument/codeAction`. Compiler-supplied fixes only; the plugin does no language-aware reasoning.
- **Document outline (Structure view, breadcrumbs)** — `textDocument/documentSymbol`.
- **Workspace symbol search (`Cmd+O` / `Ctrl+Shift+N`)** — `workspace/symbol`. The server walks every `.aww` under the workspace folders received at `initialize`.

## Install

1. Install the Awsum compiler (see [awsum-lang/awsum](https://github.com/awsum-lang/awsum)) and ensure `awsum` is on your `PATH`.
2. Install the plugin from the [JetBrains Marketplace](https://plugins.jetbrains.com/) (search "Awsum"), or sideload a locally-built `.zip`: Settings → Plugins → ⚙ → `Install Plugin from Disk…` → pick `build/distributions/awsum-intellij-A.B.C.zip`.

If `awsum` is not on your `PATH`, IntelliJ inherits `PATH` from the shell that launched it. Either launch IDEA from a terminal where `awsum` resolves, or use Toolbox / Spotlight with a `PATH` configured via your shell's startup files. There is no extension-level setting for the binary path today.

## Actions

| Action                                  | Default keybinding | What it does                                                                                  |
| --------------------------------------- | ------------------ | --------------------------------------------------------------------------------------------- |
| `Restart Awsum LSP server`              | —                  | Stops the `awsum lsp` process and starts a new one with the same settings. Useful after a local `stack install` of a new `awsum` build, or to clear any in-memory state on the server. |

Invoke via `Help` → `Find Action…` (`Cmd+Shift+A` / `Ctrl+Shift+A`) and type the action name. Bind it to a keybinding via Settings → Keymap if you use it often.

## Versioning

`awsum-intellij A.B.C` is built and tested against `awsum A.B.C`. Mismatched versions are not supported — at startup the LSP server compares the plugin's expected version against its own and shows a non-blocking notification on mismatch, with an action button that opens the install page.

The lockstep policy is shared across all three IDE plugins (`awsum-vscode`, `awsum-zed`, `awsum-intellij`) — see [CHANGELOG.md](CHANGELOG.md) for the reasoning.

## Development

```bash
./gradlew buildPlugin    # build/distributions/awsum-intellij-A.B.C.zip
./gradlew runIde         # launch sandbox IDEA with the plugin loaded
./gradlew verifyPlugin   # IntelliJ Plugin Verifier
```

JDK 21 is required; `settings.gradle.kts` declares `foojay-resolver-convention` so Gradle auto-downloads Adoptium Temurin 21 if no local JDK 21 is available.

### Structure

```
src/main/kotlin/dev/awsumlang/intellij/
  AwsumLspServerSupportProvider.kt    # extension point; spawns descriptor on .aww open
  AwsumLspServerDescriptor.kt         # `awsum lsp --stdio`, version handshake, format-via-server override
  AwsumTextMateBundleProvider.kt      # exposes the bundled TextMate grammar to IDEA
src/main/resources/
  META-INF/plugin.xml                 # plugin manifest (id, vendor, extension registrations)
  textmate/awsum/                     # VSCode-style TextMate bundle (package.json + grammar + language-configuration)
```

### Architecture

- **Single source of truth.** `awsum-intellij` does no language-aware processing. All semantics live in [`awsum/src/Awsum/Lsp.hs`](https://github.com/awsum-lang/awsum) (the LSP server) and the modules it reuses (`Awsum.Diagnostic`, `Awsum.Symbols`, `Awsum.Format`, `Awsum.ElaborateLower`, …).
- **No custom providers.** Everything routes through the IntelliJ Platform's native LSP API (`com.intellij.platform.lsp.api.LspServerSupportProvider`, `ProjectWideLspServerDescriptor`, `LspCustomization`). Anything you'd want to add for IDEA is best added on the LSP server side first — that way `awsum-vscode`, `awsum-zed`, and any other LSP client benefit too.

## Related

- Compiler: [awsum-lang/awsum](https://github.com/awsum-lang/awsum)
- VSCode extension: [awsum-lang/awsum-vscode](https://github.com/awsum-lang/awsum-vscode)
- Zed extension: [awsum-lang/awsum-zed](https://github.com/awsum-lang/awsum-zed)
- Examples: [awsum-lang/awsum-examples](https://github.com/awsum-lang/awsum-examples)
- Website: [awsum-lang.org](https://awsum-lang.org)

## License

MIT — see [LICENSE](LICENSE).
