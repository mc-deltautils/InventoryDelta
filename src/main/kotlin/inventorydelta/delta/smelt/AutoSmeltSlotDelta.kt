package inventorydelta.delta.smelt

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

    fun onOpened(player: PlayerEntity, furnaceInventory: Inventory) {
        if (!InventoryDeltaConfig.isEnabled(DeltaId.AutoSmeltSlot)) return
        if (player.entityWorld.isClient) return
        if (furnaceInventory.size() <= FUEL_SLOT) return

        val blockEntityType = (furnaceInventory as? AbstractFurnaceBlockEntity)?.type
        val profile = InventoryDeltaConfig.getSmeltProfile(blockEntityType)
        val playerInventory = player.inventory

        var changed = false

        selectTemplate(profile.recipes, playerInventory)?.let { template ->
            changed = fillSlot(
                furnaceInventory,
                RECIPE_SLOT,
                template,
                playerInventory
            ) || changed
        }

        selectTemplate(profile.fuels, playerInventory)?.let { template ->
            changed = fillSlot(
                furnaceInventory,
                FUEL_SLOT,
                template,
                playerInventory
            ) || changed
        }

        if (changed) {
            furnaceInventory.markDirty()
            playerInventory.markDirty()
        }
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

    private fun fillSlot(
        inventory: Inventory,
        slotIndex: Int,
        desired: ItemStack,
        playerInventory: PlayerInventory
    ): Boolean {
        val current = inventory.getStack(slotIndex)
        if (!current.isEmpty && !ItemStack.areItemsAndComponentsEqual(current, desired)) {
            return false
        }

        val desiredCount = desired.count
        val currentCount = current.count
        val needed = desiredCount - currentCount
        if (needed <= 0) return false

        val moved = pullFromPlayerInventory(playerInventory, desired, needed)
        if (moved <= 0) return false

        val updated = if (current.isEmpty) desired.copy() else current.copy()
        val capped = (currentCount + moved).coerceAtMost(minOf(desired.maxCount, desiredCount))
        updated.count = capped
        inventory.setStack(slotIndex, updated)
        return true
    }

    private fun pullFromPlayerInventory(
        playerInventory: PlayerInventory,
        template: ItemStack,
        needed: Int
    ): Int {
        var remaining = needed

        val mainStacks = playerInventory.mainStacks
        for (slot in mainStacks.indices) {
            if (remaining <= 0) break

            val stack = mainStacks[slot]
            if (!ItemStack.areItemsAndComponentsEqual(stack, template)) continue

            val move = minOf(remaining, stack.count)
            if (move <= 0) continue

            stack.decrement(move)
            if (stack.isEmpty) {
                mainStacks[slot] = ItemStack.EMPTY
            }
            remaining -= move
        }

        return needed - remaining
    }
}
