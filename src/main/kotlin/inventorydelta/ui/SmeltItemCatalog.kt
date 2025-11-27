package inventorydelta.ui

object SmeltItemCatalog {
    data class AllowedItems(
        val recipeIds: List<String>,
        val fuelIds: List<String>
    )

    fun collect(recipeTypeKey: String): AllowedItems {
        val recipes = when (recipeTypeKey) {
            "minecraft:blast_furnace" -> BLAST_RECIPES
            "minecraft:smoker" -> SMOKER_RECIPES
            else -> FURNACE_RECIPES
        }
        val fuels = COMMON_FUELS
        return AllowedItems(recipes, fuels)
    }

    private val FURNACE_RECIPES = listOf(
        "minecraft:iron_ore",
        "minecraft:gold_ore",
        "minecraft:raw_iron",
        "minecraft:raw_gold",
        "minecraft:cobblestone",
        "minecraft:clay_ball",
        "minecraft:sand",
        "minecraft:ancient_debris",
        "minecraft:copper_ore",
        "minecraft:raw_copper",
        "minecraft:netherrack"
    )

    private val BLAST_RECIPES = listOf(
        "minecraft:raw_iron",
        "minecraft:raw_gold",
        "minecraft:raw_copper",
        "minecraft:iron_ore",
        "minecraft:gold_ore",
        "minecraft:copper_ore",
        "minecraft:deepslate_iron_ore",
        "minecraft:deepslate_gold_ore",
        "minecraft:deepslate_copper_ore"
    )

    private val SMOKER_RECIPES = listOf(
        "minecraft:beef",
        "minecraft:porkchop",
        "minecraft:chicken",
        "minecraft:mutton",
        "minecraft:rabbit",
        "minecraft:salmon",
        "minecraft:cod",
        "minecraft:potato",
        "minecraft:kelp"
    )

    private val COMMON_FUELS = listOf(
        "minecraft:coal",
        "minecraft:charcoal",
        "minecraft:oak_planks",
        "minecraft:spruce_planks",
        "minecraft:birch_planks",
        "minecraft:jungle_planks",
        "minecraft:acacia_planks",
        "minecraft:dark_oak_planks",
        "minecraft:mangrove_planks",
        "minecraft:cherry_planks",
        "minecraft:bamboo_planks",
        "minecraft:stick",
        "minecraft:oak_log",
        "minecraft:spruce_log",
        "minecraft:birch_log",
        "minecraft:jungle_log",
        "minecraft:acacia_log",
        "minecraft:dark_oak_log",
        "minecraft:mangrove_log",
        "minecraft:cherry_log",
        "minecraft:bamboo_block",
        "minecraft:dried_kelp_block",
        "minecraft:bucket_lava",
        "minecraft:blaze_rod",
        "minecraft:coal_block"
    )
}
