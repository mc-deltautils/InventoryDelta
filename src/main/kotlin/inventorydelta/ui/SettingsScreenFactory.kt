package inventorydelta.ui

import inventorydelta.config.DeltaId
import inventorydelta.config.InventoryDeltaConfig
import inventorydelta.config.SmeltItemEntry
import inventorydelta.config.defaultSmeltProfiles
import me.shedaniel.clothconfig2.api.ConfigBuilder
import me.shedaniel.clothconfig2.api.ConfigCategory
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text

object SettingsScreenFactory {
    fun create(parent: Screen?): Screen {
        val builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Text.literal("InventoryDelta Settings"))
            .setSavingRunnable { InventoryDeltaConfig.save() }

        val deltas: ConfigCategory = builder.getOrCreateCategory(Text.literal("Deltas"))
        val entryBuilder = builder.entryBuilder()

        deltas.addEntry(
            entryBuilder
                .startBooleanToggle(
                    Text.literal(DeltaId.TransferSlotRefill.label),
                    InventoryDeltaConfig.isEnabled(DeltaId.TransferSlotRefill)
                )
                .setDefaultValue(true)
                .setTooltip(Text.literal(DeltaId.TransferSlotRefill.description))
                .setSaveConsumer { enabled ->
                    InventoryDeltaConfig.setEnabled(DeltaId.TransferSlotRefill, enabled)
                }
                .build()
        )

        deltas.addEntry(
            entryBuilder
                .startBooleanToggle(
                    Text.literal(DeltaId.AutoCraftSlot.label),
                    InventoryDeltaConfig.isEnabled(DeltaId.AutoCraftSlot)
                )
                .setDefaultValue(true)
                .setTooltip(Text.literal(DeltaId.AutoCraftSlot.description))
                .setSaveConsumer { enabled ->
                    InventoryDeltaConfig.setEnabled(DeltaId.AutoCraftSlot, enabled)
                }
                .build()
        )

        deltas.addEntry(
            entryBuilder
                .startBooleanToggle(
                    Text.literal(DeltaId.AutoSmeltSlot.label),
                    InventoryDeltaConfig.isEnabled(DeltaId.AutoSmeltSlot)
                )
                .setDefaultValue(true)
                .setTooltip(Text.literal(DeltaId.AutoSmeltSlot.description))
                .setSaveConsumer { enabled ->
                    InventoryDeltaConfig.setEnabled(DeltaId.AutoSmeltSlot, enabled)
                }
                .build()
        )

        val smeltCategory = builder.getOrCreateCategory(Text.literal("AutoSmeltSlot Config"))
        val defaultProfiles = defaultSmeltProfiles()
        InventoryDeltaConfig.smeltProfileDescriptors().forEach { descriptor ->
            val profile = InventoryDeltaConfig.getSmeltProfile(descriptor.key)
            val defaultProfile = defaultProfiles[descriptor.key] ?: defaultProfiles["default"]
            val primaryRecipe = profile.primaryRecipe()
            val primaryFuel = profile.primaryFuel()
            val defaultPrimaryRecipe = defaultProfile?.primaryRecipe() ?: primaryRecipe
            val defaultPrimaryFuel = defaultProfile?.primaryFuel() ?: primaryFuel

            smeltCategory.addEntry(
                entryBuilder
                    .startStrField(
                        Text.literal("${descriptor.label} Recipe Item ID"),
                        primaryRecipe.itemId
                    )
                    .setDefaultValue(defaultPrimaryRecipe.itemId)
                    .setTooltip(Text.literal("Item ID to place in the recipe slot when this smelter opens (primary entry)."))
                    .setSaveConsumer { value ->
                        InventoryDeltaConfig.updateSmeltProfile(descriptor.key) { current ->
                            val normalized = current.normalized()
                            val recipes = normalized.recipes.toMutableList()
                            val updatedFirst = (recipes.firstOrNull() ?: SmeltItemEntry()).copy(itemId = value.trim())
                            if (recipes.isEmpty()) recipes.add(updatedFirst) else recipes[0] = updatedFirst
                            normalized.copy(recipes = recipes)
                        }
                    }
                    .build()
            )

            smeltCategory.addEntry(
                entryBuilder
                    .startIntField(
                        Text.literal("${descriptor.label} Recipe Item Count"),
                        primaryRecipe.count
                    )
                    .setDefaultValue(defaultPrimaryRecipe.count)
                    .setMin(0)
                    .setTooltip(Text.literal("How many items to move into the recipe slot (primary entry)."))
                    .setSaveConsumer { value ->
                        InventoryDeltaConfig.updateSmeltProfile(descriptor.key) { current ->
                            val normalized = current.normalized()
                            val recipes = normalized.recipes.toMutableList()
                            val updatedFirst = (recipes.firstOrNull() ?: SmeltItemEntry(itemId = primaryRecipe.itemId)).copy(count = value)
                            if (recipes.isEmpty()) recipes.add(updatedFirst) else recipes[0] = updatedFirst
                            normalized.copy(recipes = recipes)
                        }
                    }
                    .build()
            )

            smeltCategory.addEntry(
                entryBuilder
                    .startStrField(
                        Text.literal("${descriptor.label} Fuel Item ID"),
                        primaryFuel.itemId
                    )
                    .setDefaultValue(defaultPrimaryFuel.itemId)
                    .setTooltip(Text.literal("Item ID to place in the fuel slot when this smelter opens (primary entry)."))
                    .setSaveConsumer { value ->
                        InventoryDeltaConfig.updateSmeltProfile(descriptor.key) { current ->
                            val normalized = current.normalized()
                            val fuels = normalized.fuels.toMutableList()
                            val updatedFirst = (fuels.firstOrNull() ?: SmeltItemEntry()).copy(itemId = value.trim())
                            if (fuels.isEmpty()) fuels.add(updatedFirst) else fuels[0] = updatedFirst
                            normalized.copy(fuels = fuels)
                        }
                    }
                    .build()
            )

            smeltCategory.addEntry(
                entryBuilder
                    .startIntField(
                        Text.literal("${descriptor.label} Fuel Item Count"),
                        primaryFuel.count
                    )
                    .setDefaultValue(defaultPrimaryFuel.count)
                    .setMin(0)
                    .setTooltip(Text.literal("How many items to move into the fuel slot (primary entry)."))
                    .setSaveConsumer { value ->
                        InventoryDeltaConfig.updateSmeltProfile(descriptor.key) { current ->
                            val normalized = current.normalized()
                            val fuels = normalized.fuels.toMutableList()
                            val updatedFirst = (fuels.firstOrNull() ?: SmeltItemEntry(itemId = primaryFuel.itemId)).copy(count = value)
                            if (fuels.isEmpty()) fuels.add(updatedFirst) else fuels[0] = updatedFirst
                            normalized.copy(fuels = fuels)
                        }
                    }
                    .build()
            )

            smeltCategory.addEntry(
                SmeltDefinitionButtonEntry(
                    descriptor = descriptor,
                    parentScreenProvider = { MinecraftClient.getInstance().currentScreen }
                )
            )
        }

        return builder.build()
    }
}
