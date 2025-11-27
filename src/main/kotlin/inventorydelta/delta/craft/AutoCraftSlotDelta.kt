package inventorydelta.delta.craft

import inventorydelta.config.DeltaId
import inventorydelta.config.InventoryDeltaConfig
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.RecipeInputInventory
import net.minecraft.item.ItemStack

object AutoCraftSlotDelta {
    fun onCrafted(player: PlayerEntity, craftingInventory: RecipeInputInventory, preCraftStacks: List<ItemStack>) {
        if (!InventoryDeltaConfig.isEnabled(DeltaId.AutoCraftSlot)) return
        if (player.entityWorld.isClient) return
        if (preCraftStacks.size != craftingInventory.size()) return

        val playerInventory = player.inventory
        var changed = false

        for (slot in preCraftStacks.indices) {
            val template = preCraftStacks[slot]
            if (template.isEmpty) continue

            val current = craftingInventory.getStack(slot)
            if (!current.isEmpty && !ItemStack.areItemsAndComponentsEqual(current, template)) {
                continue
            }

            val desiredCount = template.count
            val currentCount = current.count
            val needed = desiredCount - currentCount
            if (needed <= 0) continue

            val moved = pullFromPlayerInventory(playerInventory, template, needed)
            if (moved <= 0) continue

            val updated = if (current.isEmpty) template.copy() else current.copy()
            val cappedCount = (currentCount + moved).coerceAtMost(minOf(template.maxCount, desiredCount))
            updated.count = cappedCount
            craftingInventory.setStack(slot, updated)
            changed = true
        }

        if (changed) {
            craftingInventory.markDirty()
            playerInventory.markDirty()
        }
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
