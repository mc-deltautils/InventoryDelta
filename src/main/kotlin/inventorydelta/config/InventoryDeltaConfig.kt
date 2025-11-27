package inventorydelta.config

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import inventorydelta.InventoryDeltaMod
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.registry.Registries
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

private val gson = GsonBuilder().setPrettyPrinting().create()
private const val DEFAULT_SMELTER_KEY = "default"

object InventoryDeltaConfig {
    private val path: Path = FabricLoader.getInstance().configDir.resolve("inventorydelta.json")

    @Volatile
    var settings: DeltaSettings = DeltaSettings()
        private set

    fun load() {
        try {
            Files.createDirectories(path.parent)
            if (Files.exists(path)) {
                Files.newBufferedReader(path).use { reader ->
                    val json = gson.fromJson(reader, JsonObject::class.java)
                    val parsed = json?.let { gson.fromJson(it, DeltaSettings::class.java) }
                    settings = mergeWithDefaults(parsed, json)
                }
            } else {
                save()
            }
        } catch (e: IOException) {
            InventoryDeltaMod.logger.warn("Failed to load config, using defaults", e)
            settings = DeltaSettings()
        }
    }

    fun save() {
        try {
            Files.createDirectories(path.parent)
            Files.newBufferedWriter(path).use { writer ->
                gson.toJson(settings, writer)
            }
        } catch (e: IOException) {
            InventoryDeltaMod.logger.warn("Failed to save config", e)
        }
    }

    fun isEnabled(id: DeltaId): Boolean =
        when (id) {
            DeltaId.AutoCraftSlot -> settings.autoCraftSlot
            DeltaId.TransferSlotRefill -> settings.transferSlotRefill
            DeltaId.AutoSmeltSlot -> settings.autoSmeltSlot
        }

    fun setEnabled(id: DeltaId, enabled: Boolean) {
        settings = when (id) {
            DeltaId.AutoCraftSlot -> settings.copy(autoCraftSlot = enabled)
            DeltaId.TransferSlotRefill -> settings.copy(transferSlotRefill = enabled)
            DeltaId.AutoSmeltSlot -> settings.copy(autoSmeltSlot = enabled)
        }
        save()
    }

    fun getSmeltProfile(type: BlockEntityType<*>?): SmeltProfile {
        val key = smelterKey(type)
        val profiles = settings.smeltProfiles
        return (
            profiles[key]
                ?: profiles[DEFAULT_SMELTER_KEY]
                ?: defaultSmeltProfiles()[DEFAULT_SMELTER_KEY].orEmpty()
            ).normalized()
    }

    fun getSmeltProfile(key: String): SmeltProfile {
        val profiles = settings.smeltProfiles
        return (profiles[key] ?: defaultSmeltProfiles()[key].orEmpty()).normalized()
    }

    fun updateSmeltProfile(key: String, update: (SmeltProfile) -> SmeltProfile) {
        val current = settings.smeltProfiles[key] ?: defaultSmeltProfiles()[key].orEmpty()
        settings = settings.copy(smeltProfiles = settings.smeltProfiles + (key to update(current).normalized()))
        save()
    }

    fun smeltProfileDescriptors(): List<SmeltProfileDescriptor> = listOf(
        SmeltProfileDescriptor(DEFAULT_SMELTER_KEY, "Any Smelter (fallback)"),
        SmeltProfileDescriptor("minecraft:furnace", "Furnace"),
        SmeltProfileDescriptor("minecraft:blast_furnace", "Blast Furnace"),
        SmeltProfileDescriptor("minecraft:smoker", "Smoker")
    )
}

private fun mergeWithDefaults(parsed: DeltaSettings?, json: JsonObject?): DeltaSettings {
    val defaults = DeltaSettings()
    val mergedProfiles = mergeSmeltProfiles(json, parsed?.smeltProfiles, defaults.smeltProfiles)
    return DeltaSettings(
        transferSlotRefill = json?.takeIf { it.has("transferSlotRefill") }?.let { parsed?.transferSlotRefill }
            ?: defaults.transferSlotRefill,
        autoCraftSlot = json?.takeIf { it.has("autoCraftSlot") }?.let { parsed?.autoCraftSlot }
            ?: defaults.autoCraftSlot,
        autoSmeltSlot = json?.takeIf { it.has("autoSmeltSlot") }?.let { parsed?.autoSmeltSlot }
            ?: defaults.autoSmeltSlot,
        smeltProfiles = mergedProfiles
    )
}

data class DeltaSettings(
    val transferSlotRefill: Boolean = true,
    val autoCraftSlot: Boolean = true,
    val autoSmeltSlot: Boolean = true,
    val smeltProfiles: Map<String, SmeltProfile> = defaultSmeltProfiles()
)

enum class DeltaId(val key: String, val label: String, val description: String) {
    TransferSlotRefill(
        key = "transfer_slot_refill",
        label = "AutoTradeSlot Delta",
        description = "Refills villager trade input slots after each trade when matching items are available."
    ),
    AutoCraftSlot(
        key = "auto_craft_slot",
        label = "AutoCraftSlot Delta",
        description = "After a recipe has been crafted, refills the corresponding crafting recipe slots from your inventory while respecting manual placements and rapid crafting."
    ),
    AutoSmeltSlot(
        key = "auto_smelt_slot",
        label = "AutoSmeltSlot Delta",
        description = "When a smelter GUI opens, automatically fills the recipe and fuel slots using your configured items for that smelter type."
    )
}

data class SmeltItemEntry(
    val itemId: String = "",
    val count: Int = 0
) {
    fun normalized(): SmeltItemEntry = copy(
        itemId = itemId.trim(),
        count = count.coerceAtLeast(0)
    )

    fun isValid(): Boolean = itemId.isNotBlank() && count > 0
}

data class SmeltProfile(
    val recipes: List<SmeltItemEntry> = listOf(
        SmeltItemEntry(itemId = "minecraft:iron_ore", count = 8)
    ),
    val fuels: List<SmeltItemEntry> = listOf(
        SmeltItemEntry(itemId = "minecraft:coal", count = 8)
    )
) {
    fun normalized(): SmeltProfile = copy(
        recipes = recipes.map { it.normalized() }.filter { it.isValid() },
        fuels = fuels.map { it.normalized() }.filter { it.isValid() }
    )

    fun primaryRecipe(): SmeltItemEntry = recipes.firstOrNull()?.normalized()
        ?: SmeltItemEntry(itemId = "minecraft:iron_ore", count = 8)

    fun primaryFuel(): SmeltItemEntry = fuels.firstOrNull()?.normalized()
        ?: SmeltItemEntry(itemId = "minecraft:coal", count = 8)
}

data class SmeltProfileDescriptor(val key: String, val label: String)

internal fun defaultSmeltProfiles(): Map<String, SmeltProfile> = mapOf(
    DEFAULT_SMELTER_KEY to SmeltProfile().normalized(),
    "minecraft:furnace" to SmeltProfile(
        recipes = listOf(SmeltItemEntry(itemId = "minecraft:iron_ore", count = 8)),
        fuels = listOf(SmeltItemEntry(itemId = "minecraft:coal", count = 8))
    ).normalized(),
    "minecraft:blast_furnace" to SmeltProfile(
        recipes = listOf(SmeltItemEntry(itemId = "minecraft:raw_iron", count = 8)),
        fuels = listOf(SmeltItemEntry(itemId = "minecraft:coal", count = 8))
    ).normalized(),
    "minecraft:smoker" to SmeltProfile(
        recipes = listOf(SmeltItemEntry(itemId = "minecraft:raw_beef", count = 8)),
        fuels = listOf(SmeltItemEntry(itemId = "minecraft:coal", count = 8))
    ).normalized()
)

private fun smelterKey(type: BlockEntityType<*>?): String {
    if (type == null) return DEFAULT_SMELTER_KEY
    val id = Registries.BLOCK_ENTITY_TYPE.getId(type) ?: return DEFAULT_SMELTER_KEY
    return id.toString()
}

private fun mergeSmeltProfiles(
    json: JsonObject?,
    parsedProfiles: Map<String, SmeltProfile>?,
    defaults: Map<String, SmeltProfile>
): Map<String, SmeltProfile> {
    val profilesJson = json?.getAsJsonObject("smeltProfiles")
    if (profilesJson == null) {
        if (parsedProfiles != null) return defaults + parsedProfiles.mapValues { it.value.normalized() }
        return defaults
    }

    val merged = defaults.toMutableMap()
    for ((key, element) in profilesJson.entrySet()) {
        val obj = element.asJsonObject
        val parsed = parsedProfiles?.get(key)
        val profile = when {
            obj.has("recipes") || obj.has("fuels") -> parsed ?: gson.fromJson(obj, SmeltProfile::class.java)
            obj.has("ingredientId") || obj.has("fuelId") -> legacyProfile(obj)
            else -> parsed
        } ?: defaults[key] ?: SmeltProfile()

        merged[key] = profile.normalized()
    }
    return merged
}

private fun legacyProfile(obj: JsonObject): SmeltProfile {
    val ingredientId = obj.get("ingredientId")?.asString ?: "minecraft:iron_ore"
    val ingredientCount = obj.get("ingredientCount")?.asInt ?: 8
    val fuelId = obj.get("fuelId")?.asString ?: "minecraft:coal"
    val fuelCount = obj.get("fuelCount")?.asInt ?: 8
    return SmeltProfile(
        recipes = listOf(SmeltItemEntry(itemId = ingredientId, count = ingredientCount)),
        fuels = listOf(SmeltItemEntry(itemId = fuelId, count = fuelCount))
    ).normalized()
}

private fun SmeltProfile?.orEmpty(): SmeltProfile = this?.normalized() ?: SmeltProfile().normalized()
