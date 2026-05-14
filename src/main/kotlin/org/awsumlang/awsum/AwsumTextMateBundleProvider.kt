package org.awsumlang.awsum

import org.jetbrains.plugins.textmate.api.TextMateBundleProvider
import java.nio.file.Files
import java.nio.file.Path

class AwsumTextMateBundleProvider : TextMateBundleProvider {
    override fun getBundles(): List<TextMateBundleProvider.PluginBundle> =
        listOf(TextMateBundleProvider.PluginBundle("Awsum", bundlePath))

    private val bundlePath: Path by lazy {
        val dir = Files.createTempDirectory("awsum-textmate-").also {
            it.toFile().deleteOnExit()
        }
        listOf(
            "package.json",
            "language-configuration.json",
            "syntaxes/awsum.tmLanguage.json",
        ).forEach { rel ->
            val target = dir.resolve(rel)
            Files.createDirectories(target.parent)
            target.toFile().deleteOnExit()
            javaClass.classLoader.getResourceAsStream("textmate/awsum/$rel")
                ?.use { input -> Files.copy(input, target) }
                ?: error("missing bundled resource: textmate/awsum/$rel")
        }
        dir
    }
}
