plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.5.0"
}

group = "de.jaimerojas"
version = "0.0.8"

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
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    patchPluginXml {
        version = project.version.toString()
        sinceBuild.set("241")
        untilBuild.set("251.*")
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
