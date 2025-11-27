# Fabric Kotlin Mod Template

Minimal Fabric mod starter targeting Minecraft 1.21.10, Kotlin 2.2.21, and Loom 1.13-SNAPSHOT. It ships without any Kotlin sources so you can drop in your own entrypoints, mixins, and assets.

## Requirements
- JDK 21 in your PATH
- Gradle wrapper (`./gradlew`) from this repo; no global Gradle required
- Minecraft 1.21.10 with Fabric Loader >=0.17.2, Fabric API 0.138.3+1.21.10, Fabric Language Kotlin 1.13.7+kotlin.2.2.21 (versions pinned in `build.gradle.kts`)

## Repository Layout
- `build.gradle.kts` — Loom + Kotlin plugins, Fabric/Minecraft/Fabric API dependencies, and resource processing that injects `mod_version` into `fabric.mod.json`.
- `settings.gradle.kts` — root project name used by IDEs and the generated JAR.
- `gradle.properties` — editable `mod_version` property; also disables Gradle configuration cache for Loom.
- `src/main/resources/fabric.mod.json` — Fabric metadata (id, name, entrypoints, dependencies).
- `src/main/resources/fkm.mixins.json` — empty placeholder mixin config; rename and fill if you add mixins.
- `gradle/wrapper/` — wrapper JAR and properties; leave checked in.
- `build/` — build outputs from previous runs; safe to delete.

## Quick Fork/Refactor Checklist
1) Fork/clone the repo and rename the folder.
2) Set the project name in `settings.gradle.kts` (`rootProject.name = "YourModName"`).
3) Pick a version string in `gradle.properties` (`mod_version = 1.0.0`) — it becomes both the Gradle project version and the `${mod_version}` placeholder inside `fabric.mod.json`.
4) Edit `src/main/resources/fabric.mod.json`:
   - Choose a lowercase `id` (e.g., `myexamplemod`) and update `name`, `description`, `authors`, and optionally `license`.
   - Point the `entrypoints.main` value(s) to your Kotlin class (e.g., `myexamplemod.MyMod`).
   - Keep dependency versions in sync with `build.gradle.kts` if you change Minecraft/Fabric versions.
5) Add Kotlin sources under `src/main/kotlin` that match the entrypoint path you declared. A minimal initializer:

```kotlin
package myexamplemod

import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory

object MyMod : ModInitializer {
    private val logger = LoggerFactory.getLogger("myexamplemod")

    override fun onInitialize() {
        logger.info("MyExampleMod initialized")
    }
}
```

6) (Optional) Enable mixins:
   - Rename `src/main/resources/fkm.mixins.json` to something like `myexamplemod.mixins.json` and fill it with your mixin package/classes.
   - Add a `"mixins": ["myexamplemod.mixins.json"]` entry to `fabric.mod.json`.
7) Adjust dependencies in `build.gradle.kts` if you target another Minecraft version (update `minecraft`, `mappings`, `fabric-loader`, `fabric-api`, and `fabric-language-kotlin` together). Yarn and Fabric versions must match the game version.
8) Update the `LICENSE` file if you are not shipping under GPL-3.0.

## Running & Building
- `./gradlew runClient` — launches a development client with your mod on the classpath.
- `./gradlew runServer` — launches a development dedicated server.
- `./gradlew build` — produces the distributable JAR in `build/libs/` with the version from `gradle.properties`.
- Loom generates IDE run configs for client/server via `runConfigs` in `build.gradle.kts`; re-import the Gradle project if they change.

## Updating Versions Safely
1) Bump the versions in `build.gradle.kts` (Minecraft, Yarn mappings, Fabric Loader/API, Fabric Language Kotlin) in lockstep.
2) Mirror the Minecraft version and loader/API minimums in `src/main/resources/fabric.mod.json` under `depends`.
3) Delete `build/` (optional) and run `./gradlew --refresh-dependencies build` to verify the new stack.

## Distributing the Mod
1) Run `./gradlew build`.
2) Grab the JAR from `build/libs/`.
3) Drop it into the `mods/` folder of a Fabric-enabled client or server that matches the Minecraft/Fabric versions declared in `fabric.mod.json`.

## Troubleshooting
- Resource placeholders not expanding? Ensure `mod_version` is set in `gradle.properties` and rerun `./gradlew processResources`.
- Missing entrypoint errors? Confirm the package/class path in `fabric.mod.json` matches the Kotlin file location under `src/main/kotlin`.
- IDE not seeing run configs? Re-import the Gradle project so Loom can generate the client/server launch tasks.
