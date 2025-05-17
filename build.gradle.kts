plugins {
    java
    jacoco
    id("org.jetbrains.intellij.platform") version "2.5.0"
}

group = "de.jaimerojas"
version = providers.environmentVariable("PLUGIN_VERSION").getOrElse("0.0.1-SNAPSHOT")

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity("2024.1.7")
        bundledPlugin("com.intellij.tasks")
    }
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    jacocoTestReport {
        reports {
            xml.required = true
            csv.required = false
            html.required = true
        }
    }

    patchPluginXml {
        pluginVersion = version.toString()
        sinceBuild = "241"
        untilBuild = "252.*"
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
        jvmArgs(listOf("-Xms512m", "-Xmx1g"))
        systemProperties(
            "org.gradle.logging.level" to "DEBUG",
            "idea.log.level" to "DEBUG",
        )
    }

    wrapper {
        gradleVersion = "8.13"
    }

    test {
        useJUnitPlatform()
    }
}
