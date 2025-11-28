package inventorydelta.delta.smelt

import inventorydelta.client.ClientInventoryMutator
import inventorydelta.config.DeltaId
import inventorydelta.config.InventoryDeltaConfig
import inventorydelta.config.SmeltItemEntry
import net.minecraft.block.entity.AbstractFurnaceBlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier

object AutoSmeltSlotDelta {
    private const val RECIPE_SLOT = 0
    private const val FUEL_SLOT = 1

    fun onOpened(player: PlayerEntity, furnaceInventory: Inventory): Boolean {
        if (!InventoryDeltaConfig.isEnabled(DeltaId.AutoSmeltSlot)) return false
        if (!player.entityWorld.isClient) return false
        if (furnaceInventory.size() <= FUEL_SLOT) return false

        return refillClientSide(player, furnaceInventory)
    }

    private fun createTemplate(itemId: String, count: Int): ItemStack? {
        if (count <= 0) return null
        val item = resolveItem(itemId) ?: return null
        val clampedCount = count.coerceAtMost(item.maxCount)
        return ItemStack(item, clampedCount)
    }

    private fun selectTemplate(entries: List<SmeltItemEntry>, playerInventory: PlayerInventory): ItemStack? {
        for (entry in entries) {
            val template = createTemplate(entry.itemId, entry.count) ?: continue
            if (hasAtLeastOne(playerInventory, template)) return template
        }
        return null
    }

    private fun hasAtLeastOne(playerInventory: PlayerInventory, template: ItemStack): Boolean {
        return playerInventory.mainStacks.any { stack ->
            !stack.isEmpty && ItemStack.areItemsAndComponentsEqual(stack, template)
        }
    }

    private fun resolveItem(id: String): Item? {
        val identifier = Identifier.tryParse(id) ?: return null
        if (!Registries.ITEM.containsId(identifier)) return null
        return Registries.ITEM.get(identifier)
    }

    private fun refillClientSide(player: PlayerEntity, furnaceInventory: Inventory): Boolean {
        val blockEntityType = (furnaceInventory as? AbstractFurnaceBlockEntity)?.type
        val profile = InventoryDeltaConfig.getSmeltProfile(blockEntityType)
        var moved = false

        selectTemplate(profile.recipes, player.inventory)?.let { template ->
            moved = ClientInventoryMutator.moveFromInventory(
                player = player,
                targetInventory = furnaceInventory,
                targetSlotIndex = RECIPE_SLOT,
                template = template,
                desiredCount = template.count,
                allowQuickMove = true
            ) || moved
        }

        selectTemplate(profile.fuels, player.inventory)?.let { template ->
            moved = ClientInventoryMutator.moveFromInventory(
                player = player,
                targetInventory = furnaceInventory,
                targetSlotIndex = FUEL_SLOT,
                template = template,
                desiredCount = template.count,
                allowQuickMove = true
            ) || moved
        }

        return moved
    }
}
