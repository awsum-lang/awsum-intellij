package org.awsumlang.awsum

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.ProjectWideLspServerDescriptor
import com.intellij.platform.lsp.api.customization.LspCustomization
import com.intellij.platform.lsp.api.customization.LspFormattingCustomizer
import com.intellij.platform.lsp.api.customization.LspFormattingSupport

class AwsumLspServerDescriptor(project: Project) :
    ProjectWideLspServerDescriptor(project, "Awsum") {

    override fun isSupportedFile(file: VirtualFile): Boolean =
        file.extension == "aww"

    override fun createCommandLine(): GeneralCommandLine =
        GeneralCommandLine("awsum", "lsp", "--stdio")

    override fun createInitializationOptions(): Any =
        mapOf(
            "expectedAwsumVersion" to BuildConfig.PLUGIN_VERSION,
            "preferButtonsOverLinks" to true,
        )

    override val lspCustomization: LspCustomization = object : LspCustomization() {
        override val formattingCustomizer: LspFormattingCustomizer = object : LspFormattingSupport() {
            override fun shouldFormatThisFileExclusivelyByServer(
                file: VirtualFile,
                ideCanFormatThisFileItself: Boolean,
                serverExplicitlyWantsToFormatThisFile: Boolean,
            ): Boolean = file.extension == "aww"
        }
    }
}
