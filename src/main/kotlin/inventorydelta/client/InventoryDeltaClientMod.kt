package inventorydelta.client

import inventorydelta.ui.SettingsScreenFactory
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW

object InventoryDeltaClientMod : ClientModInitializer {
    private lateinit var openSettings: KeyBinding

    override fun onInitializeClient() {
        openSettings = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.inventorydelta.settings",
                GLFW.GLFW_KEY_K,
                KeyBinding.Category.INVENTORY
            )
        )

        ClientTickEvents.END_CLIENT_TICK.register { client ->
            while (openSettings.wasPressed()) {
                client.setScreen(SettingsScreenFactory.create(client.currentScreen))
            }
        }
    }
}
