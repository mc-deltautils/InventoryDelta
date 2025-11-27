# InventoryDelta

Fabric + Kotlin mod that delivers focused “Delta” inventory tweaks using a consistent naming approach.

## What’s inside
- **AutoTradeSlot Delta** — After a villager/wandering trader trade, refills the two input slots from your inventory while respecting manual placements and rapid trading.
- **AutoCraftSlot Delta** — After a recipe has been crafted, refills the corresponding crafting recipe slots from your inventory while respecting manual placements and rapid crafting.
- **Settings UI** — Cloth Config screen with per-Delta toggles; available via Mod Menu and a client keybind (`K` by default, Inventory category).
- **Config storage** — `config/inventorydelta.json` holds Delta toggles, saved automatically from the UI.
- **Mixin shell + Kotlin logic** — Java mixins on `TradeOutputSlot#onTakeItem` and `CraftingResultSlot#onTakeItem` delegate to Kotlin behavior for maintainability.

## Install
1) Drop the built JAR into your Fabric `mods/` folder.  
2) Ensure Fabric API is present. Cloth Config is bundled; Mod Menu is recommended for easy access to the settings button.

## Project layout (high level)
- `src/main/kotlin/inventorydelta/InventoryDeltaMod.kt` — main entrypoint, loads config.
- `src/main/kotlin/inventorydelta/client/InventoryDeltaClientMod.kt` — client keybind + settings screen open.
- `src/main/kotlin/inventorydelta/ui/SettingsScreenFactory.kt` — Cloth Config screen.
- `src/main/kotlin/inventorydelta/delta/transfer/TransferSlotRefillDelta.kt` — core logic for the AutoTradeSlot Delta.
- `src/main/kotlin/inventorydelta/delta/craft/AutoCraftSlotDelta.kt` — core logic for the AutoCraftSlot Delta.
- `src/main/resources/fabric.mod.json` — mod metadata and entrypoints.
- `.codex/` — internal blueprints/receipts and naming guide reference.

## Contributing / adding Deltas
- Follow the naming style and add a toggle in the config/UI for each new Delta.
- Keep mixin shells minimal; delegate logic to Kotlin classes for clarity and testing.
- Maintain vanilla-friendly behavior: avoid overwrites unless unavoidable.