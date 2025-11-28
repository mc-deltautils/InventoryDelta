package inventorydelta.client

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.SlotActionType
import kotlin.math.min

/**
 * Client-side inventory mutation utility that performs slot interactions via vanilla packet
 * semantics. This keeps mutations compatible with unmodified servers by using standard
 * slot click operations rather than direct inventory edits.
 */
@Environment(EnvType.CLIENT)
object ClientInventoryMutator {
    fun moveFromInventory(
        player: PlayerEntity,
        targetInventory: Inventory,
        targetSlotIndex: Int,
        template: ItemStack,
        desiredCount: Int,
        allowQuickMove: Boolean = false
    ): Boolean {
        val clientPlayer = player as? ClientPlayerEntity ?: return false
        val interaction = MinecraftClient.getInstance().interactionManager ?: return false
        val handler = clientPlayer.currentScreenHandler

        val targetSlotId = handler.slotId(targetInventory, targetSlotIndex)
            ?: handler.fallbackNonPlayerSlot(targetSlotIndex)
            ?: handler.nonPlayerSlotByOrdinal(targetSlotIndex)
            ?: return false
        val targetSlot = handler.getSlot(targetSlotId)
        val targetStack = targetSlot.stack
        if (!targetStack.isEmpty && !ItemStack.areItemsAndComponentsEqual(targetStack, template)) {
            return false
        }

        val needed = desiredCount - targetStack.count
        if (needed <= 0) return false

        var remaining = needed
        val candidateSlots = handler.slots.withIndex().filter { entry ->
            val slot = entry.value
            slot.inventory is PlayerInventory &&
                !slot.stack.isEmpty &&
                ItemStack.areItemsAndComponentsEqual(slot.stack, template)
        }

        for (entry in candidateSlots) {
            if (remaining <= 0) break
            val sourceSlotId = entry.index
            val sourceStack = entry.value.stack
            val toMove = min(remaining, sourceStack.count)
            if (toMove <= 0) continue

            // Pick up the full source stack.
            interaction.clickSlot(handler.syncId, sourceSlotId, 0, SlotActionType.PICKUP, clientPlayer)

            // Place the needed amount one item at a time to avoid overfilling.
            var placed = 0
            while (placed < toMove) {
                interaction.clickSlot(handler.syncId, targetSlotId, 1, SlotActionType.PICKUP, clientPlayer)
                placed++
            }

            // Return any remainder to the source slot.
            interaction.clickSlot(handler.syncId, sourceSlotId, 0, SlotActionType.PICKUP, clientPlayer)
            remaining -= placed
        }

        if (remaining < needed) return true

        if (allowQuickMove) {
            for (entry in candidateSlots) {
                val sourceSlotId = entry.index
                val before = handler.getSlot(targetSlotId).stack.copy()
                interaction.clickSlot(handler.syncId, sourceSlotId, 0, SlotActionType.QUICK_MOVE, clientPlayer)
                val after = handler.getSlot(targetSlotId).stack
                if (!ItemStack.areItemsAndComponentsEqual(before, after) || after.count != before.count) {
                    return true
                }
            }
        }

        return false
    }

    private fun ScreenHandler.slotId(inventory: Inventory, slotIndex: Int): Int? {
        return slots.indexOfFirst { it.inventory === inventory && it.index == slotIndex }
            .takeIf { it >= 0 }
    }

    private fun ScreenHandler.fallbackNonPlayerSlot(slotIndex: Int): Int? {
        return slots.indexOfFirst { slot ->
            slot.inventory !is PlayerInventory && slot.index == slotIndex
        }.takeIf { it >= 0 }
    }

    private fun ScreenHandler.nonPlayerSlotByOrdinal(ordinal: Int): Int? {
        var count = 0
        for (i in slots.indices) {
            val slot = slots[i]
            if (slot.inventory is PlayerInventory) continue
            if (count == ordinal) return i
            count++
        }
        return null
    }
}
