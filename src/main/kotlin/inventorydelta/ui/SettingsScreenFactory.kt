package inventorydelta.ui

import inventorydelta.config.DeltaId
import inventorydelta.config.InventoryDeltaConfig
import me.shedaniel.clothconfig2.api.ConfigBuilder
import me.shedaniel.clothconfig2.api.ConfigCategory
import net.minecraft.client.gui.screen.Screen
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

        return builder.build()
    }
}
