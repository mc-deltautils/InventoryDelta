# InventoryDelta

InventoryDelta is a minimal Fabric Kotlin mod configured around the InventoryDelta naming guide for focused inventory transformations. It targets Minecraft 1.21.10 with Kotlin 2.2.21 and Loom 1.13-SNAPSHOT.

## Naming Approach
- Deltas use `<Context><Target><Action>[Qualifier] Delta`.
- Context options: Inventory, Crafting, Hotbar, Pickup, Transfer.
- Target options: Slot, Grid, Stack, Item, Result.
- Action options: Refill, Sync, Merge, Filter, Lock, Shift.
- Qualifiers: Fast, Strict, Smart, Soft, Auto (optional).
- Examples: `CraftingSlotRefill Delta`, `PickupStackMerge Delta`, `TransferSlotStackSync Delta`.
- Full guidance lives in `inventorydelta_naming_schema.md` (treated as the naming guide).

## Requirements
- JDK 21 in PATH.
- Gradle wrapper (`./gradlew`) included here.
- Minecraft 1.21.10 with Fabric Loader >=0.17.2, Fabric API 0.138.3+1.21.10, Fabric Language Kotlin 1.13.7+kotlin.2.2.21 (versions pinned in `build.gradle.kts`).
- Cloth Config 20.0.149 (bundled) and Mod Menu 16.0.0-rc.1 if you want the in-game settings entry.

## Project Layout
- `build.gradle.kts` — Loom + Kotlin plugins, dependencies, resource processing for `${mod_version}`.
- `settings.gradle.kts` — root project name `InventoryDelta`.
- `gradle.properties` — `mod_version` (default `v0.0-Alpha`) and Gradle toggles.
- `src/main/kotlin/inventorydelta/InventoryDeltaMod.kt` — entrypoint logging that InventoryDelta loaded.
- `src/main/resources/fabric.mod.json` — metadata (id `inventorydelta`, entrypoint, dependencies).
- `src/main/resources/inventorydelta.mixins.json` — placeholder mixin config ready for future mixins.
- `inventorydelta_naming_schema.md` — naming guide for Delta names.

## Working With Deltas
- Use the naming approach when adding features (e.g., `InventoryStackMergeFast Delta` for a fast stack merge).
- Keep public descriptions focused on final Delta names and behaviors.
- Add new Context/Action tokens only when introducing genuinely new behaviors.

## Running & Building
- `./gradlew runClient` — dev client with InventoryDelta on the classpath.
- `./gradlew runServer` — dev server.
- `./gradlew build` — builds the JAR into `build/libs/` using the version from `gradle.properties`.

## Updating Versions
- Update versions in `build.gradle.kts` together (Minecraft, Yarn, Fabric Loader/API, Fabric Language Kotlin).
- Mirror minimums in `src/main/resources/fabric.mod.json` under `depends`.
- Optional: clean `build/` and run `./gradlew --refresh-dependencies build`.

## Distributing
- Run `./gradlew build` and drop the resulting JAR into a Fabric-enabled `mods/` folder that matches the declared versions.
