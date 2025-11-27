package inventorydelta.ui

import inventorydelta.config.InventoryDeltaConfig
import inventorydelta.config.SmeltItemEntry
import inventorydelta.config.SmeltProfileDescriptor
import inventorydelta.config.defaultSmeltProfiles
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.text.Text
import kotlin.math.max

class SmeltDefinitionPickerScreen(
    private val parent: Screen?,
    private val descriptor: SmeltProfileDescriptor
) : Screen(Text.literal("AutoSmelt Definition - ${descriptor.label}")) {

    private data class Row(
        val id: String,
        val stack: ItemStack,
        var selected: Boolean,
        val countField: TextFieldWidget,
        var x: Int = 0,
        var y: Int = 0
    )

    private val recipeRows = mutableListOf<Row>()
    private val fuelRows = mutableListOf<Row>()
    private var scrollOffset = 0
    private var maxScroll = 0
    private val footerHeight = 40
    private val buttonHeight = 20
    private val topMargin = 30
    private lateinit var saveButton: ButtonWidget
    private lateinit var cancelButton: ButtonWidget

    override fun init() {
        recipeRows.clear()
        fuelRows.clear()

        val profile = InventoryDeltaConfig.getSmeltProfile(descriptor.key)
        val defaults = defaultSmeltProfiles()
        val defaultProfile = defaults[descriptor.key] ?: defaults["default"] ?: profile

        val existingRecipeCounts = profile.recipes.associate { it.itemId to it.count }
        val existingFuelCounts = profile.fuels.associate { it.itemId to it.count }

        val allowed = SmeltItemCatalog.collect(descriptor.key)
        buildRows(
            allowed.recipeIds,
            existingRecipeCounts,
            defaultProfile.recipes,
            recipeRows
        )
        buildRows(
            allowed.fuelIds,
            existingFuelCounts,
            defaultProfile.fuels,
            fuelRows
        )

        addControlButtons()
        layoutRows()
    }

    private fun buildRows(
        ids: List<String>,
        existingCounts: Map<String, Int>,
        fallback: List<SmeltItemEntry>,
        target: MutableList<Row>
    ) {
        val client = MinecraftClient.getInstance()
        ids.forEach { id ->
            val stack = toStack(id) ?: return@forEach
            val existingCount = existingCounts[id] ?: fallback.firstOrNull { it.itemId == id }?.count ?: 8
            val countField = TextFieldWidget(client.textRenderer, 0, 0, 42, 20, Text.empty()).apply {
                text = existingCount.toString()
                setChangedListener { newValue ->
                    if (newValue.any { !it.isDigit() }) {
                        text = newValue.filter { it.isDigit() }
                    }
                }
            }
            target.add(Row(id, stack, existingCounts.containsKey(id), countField))
        }
    }

    private fun layoutRows() {
        val centerX = width / 2
        val columnWidth = 190
        val countWidth = 50
        val rowHeight = 26
        val columnSpacing = 12
        val totalRowWidth = columnWidth + countWidth + 6
        val viewHeight = (height - topMargin - footerHeight).coerceAtLeast(0)

        fun placeRows(rows: List<Row>, startY: Int) {
            rows.forEachIndexed { index, row ->
                val column = index % 2
                val rowIndex = index / 2
                val x = centerX - totalRowWidth - columnSpacing / 2 + column * (totalRowWidth + columnSpacing)
                val y = startY + rowIndex * rowHeight - scrollOffset
                row.x = x
                row.y = y
                row.countField.x = x + columnWidth + 6
                row.countField.y = y + 2
            }
        }

        val recipeStartY = topMargin
        placeRows(recipeRows, recipeStartY)
        val recipeRowsHeight = if (recipeRows.isEmpty()) 0 else ((recipeRows.size + 1) / 2) * rowHeight
        val fuelStartY = recipeStartY + recipeRowsHeight + 40
        placeRows(fuelRows, fuelStartY)

        val totalContentHeight = fuelStartY + ((fuelRows.size + 1) / 2) * rowHeight - topMargin
        maxScroll = (totalContentHeight - viewHeight).coerceAtLeast(0)

        updateButtonPositions()

        // Register children
        clearChildren()
        addDrawableChild(saveButton)
        addDrawableChild(cancelButton)
        recipeRows.forEach { addSelectableChild(it.countField) }
        fuelRows.forEach { addSelectableChild(it.countField) }
    }

    private fun addControlButtons() {
        saveButton = ButtonWidget.builder(Text.literal("Save")) {
            saveAndClose()
        }.dimensions(10, 0, 70, buttonHeight).build()

        cancelButton = ButtonWidget.builder(Text.literal("Cancel")) {
            close()
        }.dimensions(width - 10 - 70, 0, 70, buttonHeight).build()

        addDrawableChild(saveButton)
        addDrawableChild(cancelButton)
    }

    private fun updateButtonPositions() {
        val buttonY = topMargin - scrollOffset - (buttonHeight + 6)
        saveButton.x = 10
        saveButton.y = buttonY
        cancelButton.x = width - 10 - cancelButton.width
        cancelButton.y = buttonY
    }

    private fun toStack(id: String): ItemStack? {
        val identifier = net.minecraft.util.Identifier.tryParse(id) ?: return null
        if (!Registries.ITEM.containsId(identifier)) return null
        val item = Registries.ITEM.get(identifier)
        return ItemStack(item)
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        context.fill(0, 0, width, height, 0xAA101010.toInt())
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 15, 0xFFFFFF)
        context.drawCenteredTextWithShadow(
            textRenderer,
            Text.literal("Select valid recipe and fuel items for ${descriptor.label}"),
            width / 2,
            30,
            0xA0A0A0
        )

        val recipeLabelX = width / 2 - 160
        val fuelLabelX = width / 2 - 160
        val recipeLabelY = topMargin - 15
        val fuelLabelY = (recipeRows.maxOfOrNull { it.y } ?: topMargin) +
            (if (recipeRows.isEmpty()) 0 else 40)
        context.drawText(textRenderer, Text.literal("Recipe Items"), recipeLabelX, recipeLabelY, 0xFFFFFF, false)
        context.drawText(textRenderer, Text.literal("Fuel Items"), fuelLabelX, fuelLabelY, 0xFFFFFF, false)

        renderRows(context, recipeRows)
        renderRows(context, fuelRows)

        super.render(context, mouseX, mouseY, delta)

        recipeRows.forEach { it.countField.render(context, mouseX, mouseY, delta) }
        fuelRows.forEach { it.countField.render(context, mouseX, mouseY, delta) }
    }

    private fun renderRows(context: DrawContext, rows: List<Row>) {
        val columnWidth = 190
        val rowHeight = 20
        rows.forEach { row ->
            val bg = if (row.selected) 0xFF2E7D32.toInt() else 0xFF3A3A3A.toInt()
            context.fill(row.x, row.y, row.x + columnWidth, row.y + rowHeight, bg)
            context.fill(row.x, row.y, row.x + columnWidth, row.y + 1, 0xFF000000.toInt())
            context.fill(row.x, row.y + rowHeight - 1, row.x + columnWidth, row.y + rowHeight, 0xFF000000.toInt())
            context.fill(row.x, row.y, row.x + 1, row.y + rowHeight, 0xFF000000.toInt())
            context.fill(row.x + columnWidth - 1, row.y, row.x + columnWidth, row.y + rowHeight, 0xFF000000.toInt())
            context.drawItem(row.stack, row.x + 2, row.y + 2)
            context.drawText(textRenderer, Text.literal(row.id), row.x + 24, row.y + 6, 0xFFFFFF, false)
        }
    }

    override fun shouldCloseOnEsc(): Boolean = true

    override fun close() {
        client?.setScreen(parent)
    }

    override fun mouseClicked(click: net.minecraft.client.gui.Click, doubleClick: Boolean): Boolean {
        val mouseX = click.x()
        val mouseY = click.y()

        recipeRows.firstOrNull { it.countField.isMouseOver(mouseX, mouseY) }?.let { row ->
            if (row.countField.mouseClicked(click, doubleClick)) {
                setFocused(row.countField)
                return true
            }
        }
        fuelRows.firstOrNull { it.countField.isMouseOver(mouseX, mouseY) }?.let { row ->
            if (row.countField.mouseClicked(click, doubleClick)) {
                setFocused(row.countField)
                return true
            }
        }

        val columnWidth = 190
        val rowHeight = 20

        fun toggle(rows: List<Row>): Boolean {
            rows.forEach { row ->
                if (mouseX >= row.x && mouseX <= row.x + columnWidth && mouseY >= row.y && mouseY <= row.y + rowHeight) {
                    row.selected = !row.selected
                    return true
                }
            }
            return false
        }

        if (toggle(recipeRows) || toggle(fuelRows)) return true
        return super.mouseClicked(click, doubleClick)
    }

    override fun charTyped(input: net.minecraft.client.input.CharInput): Boolean {
        val focusedField = focused as? TextFieldWidget
        if (focusedField != null && focusedField.charTyped(input)) return true
        return super.charTyped(input)
    }

    override fun keyPressed(input: net.minecraft.client.input.KeyInput): Boolean {
        val focusedField = focused as? TextFieldWidget
        if (focusedField != null && focusedField.keyPressed(input)) return true
        return super.keyPressed(input)
    }

    private fun saveAndClose() {
        val defaults = defaultSmeltProfiles()
        val defaultProfile = defaults[descriptor.key] ?: defaults["default"] ?: InventoryDeltaConfig.getSmeltProfile(descriptor.key)

        val selectedRecipes = recipeRows.filter { it.selected }.map {
            SmeltItemEntry(itemId = it.id, count = parseCount(it.countField.text, defaultProfile.recipes.firstOrNull { r -> r.itemId == it.id }?.count ?: 8))
        }
        val selectedFuels = fuelRows.filter { it.selected }.map {
            SmeltItemEntry(itemId = it.id, count = parseCount(it.countField.text, defaultProfile.fuels.firstOrNull { f -> f.itemId == it.id }?.count ?: 8))
        }

        InventoryDeltaConfig.updateSmeltProfile(descriptor.key) { current ->
            current.copy(
                recipes = selectedRecipes.ifEmpty { defaultProfile.recipes },
                fuels = selectedFuels.ifEmpty { defaultProfile.fuels }
            )
        }
        close()
    }

    private fun parseCount(raw: String, fallback: Int): Int {
        return raw.toIntOrNull()?.coerceAtLeast(1) ?: max(1, fallback)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        val step = 20
        scrollOffset = (scrollOffset - (verticalAmount * step).toInt()).coerceIn(0, maxScroll)
        layoutRows()
        return true
    }
}
