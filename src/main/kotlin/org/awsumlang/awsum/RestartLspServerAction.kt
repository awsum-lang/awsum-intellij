package org.awsumlang.awsum

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.platform.lsp.api.LspClientManager

/**
 * Puts "Restart Awsum LSP server" into Find Action, where it can take a keyboard shortcut. The platform's own
 * restart exists only as buttons in the Language Services widget and the Services tool window, with no action ID.
 *
 * Each running client is restarted from its own descriptor, as the platform's restart does since 2026.3, so the
 * server comes back even when no .aww tab is open. With no client yet, it starts one for the open .aww files.
 */
class RestartLspServerAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val manager = LspClientManager.getInstance(project)
        val provider = AwsumLspIntegrationProvider::class.java
        val descriptors = manager.getClients(provider).map { it.descriptor }
        manager.stopClients(provider)
        if (descriptors.isEmpty()) {
            manager.startClientsIfNeeded(provider)
        } else {
            descriptors.forEach { manager.ensureClientStarted(provider, it) }
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}
