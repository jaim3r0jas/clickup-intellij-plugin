plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.4"
}

group = "de.safenow.clickup"
version = "1.0"

repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2024.1.7")
//    type.set("IC") // Target IDE Platform
    type.set("IU") // Target IDE Platform

    plugins.set(listOf("tasks"))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    patchPluginXml {
        sinceBuild.set("241")
        untilBuild.set("243.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }

    runIde {
        jvmArgs(listOf("-Xms512m", "-Xmx1g"))
        systemProperties(
            "org.gradle.logging.level" to "DEBUG",
            // increase loggingh level to see all intellij Logger is logging
            "idea.log.level" to "DEBUG"
        )
    }

    wrapper {
        gradleVersion = "8.13"
    }

    test {
        useJUnitPlatform()
    }
}
