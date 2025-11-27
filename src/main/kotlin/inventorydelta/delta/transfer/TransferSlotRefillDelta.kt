package inventorydelta.delta.transfer

import inventorydelta.config.DeltaId
import inventorydelta.config.InventoryDeltaConfig
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.screen.MerchantScreenHandler
import net.minecraft.village.Merchant
import net.minecraft.village.MerchantInventory
import net.minecraft.village.TradeOffer

object TransferSlotRefillDelta {
    private const val FIRST_INPUT_SLOT = 0
    private const val SECOND_INPUT_SLOT = 1

    fun onTradeCompleted(
        merchantInventory: MerchantInventory,
        player: PlayerEntity,
        merchant: Merchant,
        tradeOffer: TradeOffer? = null
    ) {
        if (!InventoryDeltaConfig.isEnabled(DeltaId.TransferSlotRefill)) return
        if (player.entityWorld.isClient) return
        if (merchant.customer != player) return
        if (player.currentScreenHandler !is MerchantScreenHandler) return

        val offer: TradeOffer = tradeOffer ?: merchantInventory.tradeOffer ?: return

        val firstBuy = offer.displayedFirstBuyItem
        val secondBuy = offer.displayedSecondBuyItem
        val playerInventory = player.inventory

        var changed = applyRefill(merchantInventory, FIRST_INPUT_SLOT, firstBuy, playerInventory)
        if (!secondBuy.isEmpty) {
            changed = applyRefill(merchantInventory, SECOND_INPUT_SLOT, secondBuy, playerInventory) || changed
        }

        if (changed) {
            merchantInventory.markDirty()
            playerInventory.markDirty()
        }
    }

    private fun applyRefill(
        merchantInventory: MerchantInventory,
        slotIndex: Int,
        required: ItemStack,
        playerInventory: PlayerInventory
    ): Boolean {
        if (required.isEmpty) return false

        val current = merchantInventory.getStack(slotIndex)
        if (!current.isEmpty && !ItemStack.areItemsAndComponentsEqual(current, required)) {
            return false
        }

        val currentCount = current.count
        val desiredCount = required.count
        val needed = desiredCount - currentCount
        if (needed <= 0) return false

        val moved = pullFromPlayerInventory(playerInventory, required, needed)
        if (moved <= 0) return false

        val cappedCount = (currentCount + moved).coerceAtMost(minOf(required.maxCount, desiredCount))
        val updated = if (current.isEmpty) required.copy() else current.copy()
        updated.count = cappedCount
        merchantInventory.setStack(slotIndex, updated)
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
