// '`FabricKotlinMod`'/build.gradle.kts
import org.gradle.language.jvm.tasks.ProcessResources

plugins {
    id("fabric-loom") version "1.13-SNAPSHOT"
    kotlin("jvm") version "2.2.21"
}

val modVersion = project.property("mod_version") as String

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
}

dependencies {
    // Minecraft + Yarn mappings for 1.21.10
    minecraft("com.mojang:minecraft:1.21.10")
    mappings("net.fabricmc:yarn:1.21.10+build.2")

    // Fabric Loader + Fabric API (latest stable for 1.21.10)
    modImplementation("net.fabricmc:fabric-loader:0.17.2")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.138.3+1.21.10")

    // Fabric Language Kotlin (latest, tied to Kotlin 2.2.21)
    modImplementation("net.fabricmc:fabric-language-kotlin:1.13.7+kotlin.2.2.21")
}

kotlin {
    jvmToolchain(21)
}

version = modVersion

loom {
    runConfigs {
        named("client") { ideConfigGenerated(true) }
        named("server") { ideConfigGenerated(true) }
    }

}

tasks.named<ProcessResources>("processResources") {
    inputs.property("mod_version", modVersion)
    filesMatching("fabric.mod.json") {
        expand("mod_version" to modVersion)
    }
}
