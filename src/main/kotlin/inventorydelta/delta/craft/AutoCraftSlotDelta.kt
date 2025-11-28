package inventorydelta.delta.craft

import inventorydelta.config.DeltaId
import inventorydelta.config.InventoryDeltaConfig
import inventorydelta.client.ClientInventoryMutator
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.RecipeInputInventory
import net.minecraft.item.ItemStack
import net.minecraft.util.collection.DefaultedList

object AutoCraftSlotDelta {
    fun snapshotInputs(craftingInventory: RecipeInputInventory): DefaultedList<ItemStack> {
        val size = craftingInventory.size()
        val copy = DefaultedList.ofSize(size, ItemStack.EMPTY)
        for (slot in 0 until size) {
            copy[slot] = craftingInventory.getStack(slot).copy()
        }
        return copy
    }

    fun onCrafted(player: PlayerEntity, craftingInventory: RecipeInputInventory, preCraftStacks: List<ItemStack>) {
        if (!InventoryDeltaConfig.isEnabled(DeltaId.AutoCraftSlot)) return
        if (preCraftStacks.size != craftingInventory.size()) return

        if (player.entityWorld.isClient) {
            refillClientSide(player, craftingInventory, preCraftStacks)
            return
        }

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

    private fun refillClientSide(
        player: PlayerEntity,
        craftingInventory: RecipeInputInventory,
        preCraftStacks: List<ItemStack>
    ) {
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

            ClientInventoryMutator.moveFromInventory(
                player = player,
                targetInventory = craftingInventory,
                targetSlotIndex = slot,
                template = template,
                desiredCount = desiredCount
            )
        }
    }
}
