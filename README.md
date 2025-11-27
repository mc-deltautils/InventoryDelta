# InventoryDelta

Fabric + Kotlin mod that delivers focused “Delta” inventory tweaks using a consistent naming approach. Targets Minecraft **1.21.10** with Fabric Loader **0.18.1**, Fabric API **0.138.3+1.21.10**, Fabric Language Kotlin **1.13.7+kotlin.2.2.21**, and Loom **1.13-SNAPSHOT**.

## What’s inside
- **TransferSlotRefill Delta** — After a villager/wandering trader trade, refills the two input slots from your inventory while respecting manual placements and rapid trading.
- **Settings UI** — Cloth Config screen with per-Delta toggles; available via Mod Menu and a client keybind (`K` by default, Inventory category).
- **Config storage** — `config/inventorydelta.json` holds Delta toggles, saved automatically from the UI.
- **Mixin shell + Kotlin logic** — Java mixin on `TradeOutputSlot#onTakeItem` delegates to Kotlin behavior for maintainability.

## Naming style
All features follow `<Context><Target><Action>[Qualifier] Delta` (e.g., `TransferSlotRefill Delta`). Full guide: `inventorydelta_naming_schema.md`.

## Install
1) Drop the built JAR into your Fabric `mods/` folder.  
2) Ensure Fabric API is present. Cloth Config is bundled; Mod Menu is recommended for easy access to the settings button.

## Build & dev
- `./gradlew build` — produces a remapped JAR in `build/libs/`.
- `./gradlew runClient` / `./gradlew runServer` — dev run configs.

## Project layout (high level)
- `src/main/kotlin/inventorydelta/InventoryDeltaMod.kt` — main entrypoint, loads config.
- `src/main/kotlin/inventorydelta/client/InventoryDeltaClientMod.kt` — client keybind + settings screen open.
- `src/main/kotlin/inventorydelta/ui/SettingsScreenFactory.kt` — Cloth Config screen.
- `src/main/kotlin/inventorydelta/delta/transfer/TransferSlotRefillDelta.kt` — core logic for the trade refill Delta.
- `src/main/resources/fabric.mod.json` — mod metadata and entrypoints.
- `.codex/` — internal blueprints/receipts and naming guide reference.

## Contributing / adding Deltas
- Follow the naming style and add a toggle in the config/UI for each new Delta.
- Keep mixin shells minimal; delegate logic to Kotlin classes for clarity and testing.
- Maintain vanilla-friendly behavior: avoid overwrites unless unavoidable.
