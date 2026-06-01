plugins {
    id("java")
    kotlin("jvm") version "2.3.21"
    id("org.jetbrains.intellij.platform") version "2.16.0"
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        create(
            providers.gradleProperty("platformType").get(),
            providers.gradleProperty("platformVersion").get(),
        )
        bundledPlugin("org.jetbrains.plugins.textmate")
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
            untilBuild = provider { null }
        }
    }
}

// LICENSE/NOTICE are not auto-bundled into IntelliJ plugin JARs; copy them
// into META-INF/ so they ship with the artefact.
tasks.processResources {
    from(layout.projectDirectory.file("LICENSE")) { into("META-INF") }
    from(layout.projectDirectory.file("NOTICE")) { into("META-INF") }
}

val generatedSourcesDir = layout.buildDirectory.dir("generated/sources/buildConfig/kotlin/main")

val generateBuildConfig by tasks.registering {
    val outputDir = generatedSourcesDir
    val pluginVersion = providers.gradleProperty("pluginVersion")
    inputs.property("pluginVersion", pluginVersion)
    outputs.dir(outputDir)
    doLast {
        val file = outputDir.get().asFile.resolve("org/awsumlang/awsum/BuildConfig.kt")
        file.parentFile.mkdirs()
        file.writeText(
            """package org.awsumlang.awsum

internal object BuildConfig {
    const val PLUGIN_VERSION: String = "${pluginVersion.get()}"
}
"""
        )
    }
}

kotlin {
    jvmToolchain(21)
    sourceSets["main"].kotlin.srcDir(generateBuildConfig.map { generatedSourcesDir })
}
