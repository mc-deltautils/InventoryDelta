package inventorydelta.client

import inventorydelta.delta.smelt.AutoSmeltSlotDelta
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.AbstractFurnaceScreen
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.Slot

@Environment(EnvType.CLIENT)
object SmeltScreenRefillWatcher {
    private var currentSyncId: Int? = null
    private var attempts: Int = 0
    private var satisfied: Boolean = false

    fun onTick(client: MinecraftClient) {
        val screen = client.currentScreen
        if (screen !is AbstractFurnaceScreen<*>) {
            reset()
            return
        }

        val handler: ScreenHandler = screen.screenHandler
        val syncId = handler.syncId
        if (currentSyncId != syncId) {
            currentSyncId = syncId
            attempts = 0
            satisfied = false
        }

        if (satisfied) return
        if (attempts > 40) { // ~2 seconds at 20 TPS
            satisfied = true
            return
        }
        attempts++

        val player = client.player ?: return
        val furnaceInventory = findFurnaceInventory(handler) ?: return

        val moved = AutoSmeltSlotDelta.onOpened(player, furnaceInventory)
        if (moved) {
            satisfied = true
        }
    }

    private fun findFurnaceInventory(handler: ScreenHandler): Inventory? {
        for (slot: Slot in handler.slots) {
            if (slot.inventory !is PlayerInventory) {
                return slot.inventory
            }
        }
        return null
    }

    private fun reset() {
        currentSyncId = null
        attempts = 0
        satisfied = false
    }
}
