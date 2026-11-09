# Awsum language support for IntelliJ IDEA

IntelliJ Platform plugin for the [Awsum](https://awsum-lang.org) programming language (`.aww` files). A thin LSP client to the `awsum lsp` server bundled with the Awsum compiler — every diagnostic, code action, format edit, hover, document symbol, and workspace symbol is computed inside the `awsum` compiler binary and pushed over LSP.

Works in IntelliJ IDEA (free tier) and every Ultimate-tier JetBrains IDE (Rider, CLion, WebStorm, PyCharm Pro, GoLand, …). Requires IDEA 2026.2 or newer.

## Features

- **Syntax highlighting** — TextMate grammar shared with [`awsum-vscode`](https://github.com/awsum-lang/awsum-vscode).
- **Formatting (Reformat Code)** — `textDocument/formatting`, same algorithm as `awsum format`. To format on save, turn on Settings → Tools → Actions on Save → Reformat code.
- **Inline diagnostics** — `textDocument/publishDiagnostics`, pushed on open / save / change (debounced 500 ms server-side). `error` vs `warning` severity honoured by the IDEA Problems panel.
- **Quick fixes (intention actions)** — `textDocument/codeAction`. Compiler-supplied fixes only; the plugin does no language-aware reasoning.
- **Hover / Quick Documentation** — `textDocument/hover`. The type of the name under the cursor (for a polymorphic reference, also its type at that call site) and its doc comment, for user and prelude names alike.
- **Document outline (Structure view, breadcrumbs)** — `textDocument/documentSymbol`.
- **Workspace symbol search (`Cmd+O` / `Ctrl+Shift+N`)** — `workspace/symbol`. The server walks every `.aww` under the workspace folders received at `initialize`.

## Install

1. Install the Awsum compiler (see [awsum-lang/awsum](https://github.com/awsum-lang/awsum)) and ensure `awsum` is on your `PATH`.
2. Install the plugin from the [JetBrains Marketplace](https://plugins.jetbrains.com/) (search "Awsum"), or sideload a locally-built `.zip`: Settings → Plugins → ⚙ → `Install Plugin from Disk…` → pick `build/distributions/awsum-intellij-A.B.C.zip`.

If `awsum` is not on your `PATH`, IntelliJ inherits `PATH` from the shell that launched it. Either launch IDEA from a terminal where `awsum` resolves, or use Toolbox / Spotlight with a `PATH` configured via your shell's startup files. There is no extension-level setting for the binary path today.

## Actions

| Action                                  | Default keybinding | What it does                                                                                  |
| --------------------------------------- | ------------------ | --------------------------------------------------------------------------------------------- |
| `Restart Awsum LSP server`              | —                  | Stops the `awsum lsp` process and starts a new one with the same settings, even when no `.aww` tab is open. Useful after a local `stack install` of a new `awsum` build, or to clear any in-memory state on the server. |

Invoke via `Help` → `Find Action…` (`Cmd+Shift+A` / `Ctrl+Shift+A`) and type the action name. Bind it to a keybinding via Settings → Keymap if you use it often. The Restart button in the Language Services status bar widget does the same by mouse, but it has no action ID, so Find Action and Keymap can't reach it — this action is what they reach.

## Versioning

`awsum-intellij A.B.C` is built and tested against `awsum A.B.C`. Mismatched versions are not supported — at startup the LSP server compares the plugin's expected version against its own and shows a non-blocking notification on mismatch, with an action button that opens the install page.

The lockstep policy is shared across all five editor plugins (`awsum-vscode`, `awsum-zed`, `awsum-intellij`, `awsum-nvim`, `awsum-emacs`) — see [CHANGELOG.md](CHANGELOG.md) for the reasoning.

Supported editor versions follow Awsum's [platform version policy](https://github.com/awsum-lang/awsum/blob/main/docs/platform-version-policy.md#editors): the minimum is the newest stable release that has been out for at least 7 days.

## Development

```bash
./gradlew buildPlugin    # build/distributions/awsum-intellij-A.B.C.zip
./gradlew runIde         # launch sandbox IDEA with the plugin loaded
./gradlew verifyPlugin   # IntelliJ Plugin Verifier
```

JDK 21 is required; `settings.gradle.kts` declares `foojay-resolver-convention` so Gradle auto-downloads Adoptium Temurin 21 if no local JDK 21 is available.

### Structure

```
src/main/kotlin/org/awsumlang/awsum/
  AwsumLspIntegrationProvider.kt      # extension point; spawns descriptor on .aww open
  AwsumLspClientDescriptor.kt         # `awsum lsp --stdio`, version handshake, format-via-server override
  RestartLspServerAction.kt           # "Restart Awsum LSP server" action
  AwsumTextMateBundleProvider.kt      # exposes the bundled TextMate grammar to IDEA
src/main/resources/
  META-INF/plugin.xml                 # plugin manifest (id, vendor, extension registrations)
  textmate/awsum/                     # VSCode-style TextMate bundle (package.json + grammar + language-configuration)
```

### Architecture

- **Single source of truth.** `awsum-intellij` does no language-aware processing. All semantics live in [`awsum/src/Awsum/Lsp.hs`](https://github.com/awsum-lang/awsum) (the LSP server) and the modules it reuses (`Awsum.Diagnostic`, `Awsum.Symbols`, `Awsum.Format`, `Awsum.ElaborateLower`, …).
- **No custom providers.** Everything routes through the IntelliJ Platform's native LSP API (`com.intellij.platform.lsp.api.LspIntegrationProvider`, `ProjectWideLspClientDescriptor`, `LspCustomization`) — the names IDEA 2026.2 introduced, which put the floor of `pluginSinceBuild` at 262. Anything you'd want to add for IDEA is best added on the LSP server side first — that way `awsum-vscode`, `awsum-zed`, and any other LSP client benefit too.

## Related

- Compiler: [awsum-lang/awsum](https://github.com/awsum-lang/awsum)
- VSCode extension: [awsum-lang/awsum-vscode](https://github.com/awsum-lang/awsum-vscode)
- Zed extension: [awsum-lang/awsum-zed](https://github.com/awsum-lang/awsum-zed)
- Examples: [awsum-lang/awsum-examples](https://github.com/awsum-lang/awsum-examples)
- Website: [awsum-lang.org](https://awsum-lang.org)

## AI use

This IntelliJ Platform plugin is developed with substantial usage of generative AI.

## License

Apache 2.0 — see [LICENSE](LICENSE) and [NOTICE](NOTICE).
