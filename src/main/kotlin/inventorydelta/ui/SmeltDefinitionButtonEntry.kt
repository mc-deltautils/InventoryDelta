package inventorydelta.ui

import inventorydelta.config.SmeltProfileDescriptor
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.Selectable
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.text.Text
import java.util.Optional

class SmeltDefinitionButtonEntry(
    private val descriptor: SmeltProfileDescriptor,
    private val parentScreenProvider: () -> Screen?
) : AbstractConfigListEntry<Unit>(Text.literal("Define Recipe/Fuel"), false) {

    private val button: ButtonWidget = ButtonWidget.builder(
        Text.literal("Define Recipe/Fuel")
    ) {
        val parent = parentScreenProvider() ?: MinecraftClient.getInstance().currentScreen
        MinecraftClient.getInstance().setScreen(SmeltDefinitionPickerScreen(parent, descriptor))
    }.dimensions(0, 0, 200, 20).build()

    override fun getItemHeight(): Int = 24

    override fun children(): MutableList<Element> = mutableListOf(button)

    override fun narratables(): MutableList<Selectable> = mutableListOf(button)

    override fun render(
        context: DrawContext,
        index: Int,
        y: Int,
        x: Int,
        entryWidth: Int,
        entryHeight: Int,
        mouseX: Int,
        mouseY: Int,
        hovered: Boolean,
        delta: Float
    ) {
        button.x = x
        button.y = y + (entryHeight - button.height) / 2
        button.width = entryWidth
        button.render(context, mouseX, mouseY, delta)
    }

    override fun isEdited(): Boolean = false

    override fun getValue(): Unit = Unit

    override fun getDefaultValue(): Optional<Unit> = Optional.empty()
}
