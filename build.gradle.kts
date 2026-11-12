import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType

plugins {
    id("java") // Java support
    // No Kotlin sources, but Kover only attaches its coverage agent to the tests when the Kotlin JVM plugin is applied:
    // without it the coverage report is empty and koverVerify passes on nothing.
    alias(libs.plugins.kotlin)
    alias(libs.plugins.intelliJPlatform) // IntelliJ Platform Gradle Plugin
    alias(libs.plugins.changelog) // Gradle Changelog Plugin
    alias(libs.plugins.qodana) // Gradle Qodana Plugin
    alias(libs.plugins.kover) // Gradle Kover Plugin
}

group = "de.jaimerojas"
version = providers.environmentVariable("PLUGIN_VERSION").getOrElse("0.0.1-SNAPSHOT")

// The oldest IDE the plugin supports: 2024.3, about two years of releases. The bytecode targets Java 21, which every
// IDE from 2024.2 on runs.
val pluginSinceBuild = "243"
// The newest stable IDE release: the plugin compiles against it and the Plugin Verifier checks it.
val platformVersion = "2026.3"

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

kover {
    reports {
        filters {
            excludes {
                // Add exclusions as needed
                classes("*.BuildConfig", "*.*Test*")
            }
        }

        verify {
            rule {
                minBound(20)
            }
        }
    }
}

dependencies {
    intellijPlatform {
        intellijIdea(platformVersion)
        // The Tasks API. Up to 2025.3 it is the bundled plugin com.intellij.tasks. From 2026.3 on, its core (Task,
        // TaskRepository, TaskRepositoryType) is the bundled com.intellij.platform.tasks, and the repository base classes and the
        // commit placeholders are "Issue Trackers" (still com.intellij.tasks), a Marketplace plugin: compatiblePlugin
        // resolves its release for platformVersion.
        bundledPlugin("com.intellij.platform.tasks")
        compatiblePlugin("com.intellij.tasks")
    }
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
    // The IntelliJ test environment loads JUnit 4 classes even for JUnit 5 tests
    testRuntimeOnly(libs.junit4)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junit.jupiter)
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = pluginSinceBuild
            untilBuild = provider { null }
        }
    }

    pluginVerification {
        ides {
            // The oldest and the newest supported release of IntelliJ IDEA and of PhpStorm (#63 was a PhpStorm-only
            // incompatibility): an API missing from the oldest or removed by the newest shows up in one of them.
            create(IntelliJPlatformType.IntellijIdea, "2024.3")
            create(IntelliJPlatformType.IntellijIdea, platformVersion)
            create(IntelliJPlatformType.PhpStorm, "2024.3")
            create(IntelliJPlatformType.PhpStorm, platformVersion)
        }
    }
}

tasks {
    withType<JavaCompile> {
        options.release = 21
    }

    patchPluginXml {
        pluginVersion = version.toString()
    }

    signPlugin {
        certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
        privateKey = providers.environmentVariable("PRIVATE_KEY")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }

    publishPlugin {
        token = providers.environmentVariable("PUBLISH_TOKEN")
    }

    runIde {
        // -Xshare:off: silences the JVM's class data sharing warning (the IDE's agents append to the boot classpath)
        jvmArgs(listOf("-Xms512m", "-Xmx1g", "-Xshare:off"))
        systemProperties(
            "org.gradle.logging.level" to "DEBUG",
            "idea.log.level" to "DEBUG",
        )
    }

    wrapper {
        gradleVersion = "9.9.1"
    }

    test {
        useJUnitPlatform()
    }
}
