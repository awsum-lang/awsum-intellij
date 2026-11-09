package org.awsumlang.awsum

import com.intellij.openapi.application.PluginPathManager
import org.jetbrains.plugins.textmate.api.TextMateBundleProvider

/**
 * The TextMate API takes a directory on disk, so the bundle ships as plain files in the plugin's distribution
 * directory (`textmate/awsum/`, placed there by build.gradle.kts) and the IDE reads it in place.
 */
class AwsumTextMateBundleProvider : TextMateBundleProvider {
    override fun getBundles(): List<TextMateBundleProvider.PluginBundle> {
        val bundle = PluginPathManager.getPluginResource(javaClass, "textmate/awsum")
            ?: error("cannot locate the plugin directory holding the TextMate bundle")
        return listOf(TextMateBundleProvider.PluginBundle("Awsum", bundle.toPath()))
    }
}
