package inventorydelta.delta

import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack

/**
 * Captures a snapshot of a slot and offers stability checks so downstream actions
 * only run once the observed state remains aligned with expectations.
 */
data class ObservedSlot(val stack: ItemStack) {
    fun isCompatibleWith(template: ItemStack): Boolean {
        if (stack.isEmpty) return true
        return ItemStack.areItemsAndComponentsEqual(stack, template)
    }

    fun applyIfUnchanged(inventory: Inventory, slotIndex: Int, updated: ItemStack): Boolean {
        val current = inventory.getStack(slotIndex)
        if (!ItemStack.areItemsAndComponentsEqual(current, stack) || current.count != stack.count) {
            return false
        }
        inventory.setStack(slotIndex, updated)
        return true
    }

    companion object {
        fun capture(inventory: Inventory, slotIndex: Int): ObservedSlot {
            return ObservedSlot(inventory.getStack(slotIndex).copy())
        }
    }
}
