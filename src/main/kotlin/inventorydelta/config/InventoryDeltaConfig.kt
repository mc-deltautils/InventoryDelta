package inventorydelta.config

import com.google.gson.GsonBuilder
import inventorydelta.InventoryDeltaMod
import net.fabricmc.loader.api.FabricLoader
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

private val gson = GsonBuilder().setPrettyPrinting().create()

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
                    settings = gson.fromJson(reader, DeltaSettings::class.java) ?: DeltaSettings()
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

    fun isEnabled(id: DeltaId): Boolean = when (id) {
        DeltaId.TransferSlotRefill -> settings.transferSlotRefill
    }

    fun setEnabled(id: DeltaId, enabled: Boolean) {
        settings = when (id) {
            DeltaId.TransferSlotRefill -> settings.copy(transferSlotRefill = enabled)
        }
        save()
    }
}

data class DeltaSettings(
    val transferSlotRefill: Boolean = true
)

enum class DeltaId(val key: String, val label: String, val description: String) {
    TransferSlotRefill(
        key = "transfer_slot_refill",
        label = "TransferSlotRefill Delta",
        description = "Refills villager trade input slots after each trade when matching items are available."
    )
}
