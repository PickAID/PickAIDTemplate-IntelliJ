plugins {
    kotlin("jvm") version "2.1.21"
    id("org.jetbrains.intellij.platform") version "2.15.0"
}

group = "cn.mihono.pickaid"
version = "0.1.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdea("2024.3.6")
        bundledPlugin("org.toml.lang")
    }

    testImplementation(kotlin("test"))
    testRuntimeOnly("junit:junit:4.13.2")
}

kotlin {
    jvmToolchain(21)
}

intellijPlatform {
    pluginConfiguration {
        id = "cn.mihono.pickaid.template.intellij"
        name = "PickAID Template Assistant"
        version = project.version.toString()

        description = """
            Editor assistance for PickAID Minecraft template project.toml files.
            Provides schema-aware completion, inspections, and focused quick fixes.
        """.trimIndent()

        ideaVersion {
            sinceBuild = "243"
        }
    }

    pluginVerification {
        ides {
            recommended()
        }
    }
}

tasks {
    test {
        useJUnitPlatform()
    }
}
