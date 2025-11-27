package inventorydelta

import inventorydelta.config.InventoryDeltaConfig
import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory

object InventoryDeltaMod : ModInitializer {
    internal val logger = LoggerFactory.getLogger("inventorydelta")

    override fun onInitialize() {
        InventoryDeltaConfig.load()
        logger.info("InventoryDelta initialized")
    }
}
