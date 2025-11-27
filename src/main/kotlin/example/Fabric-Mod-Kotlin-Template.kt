package example

import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory

object FabricKotlinMod : ModInitializer {
    private val logger = LoggerFactory.getLogger("Fabric-Mod-Kotlin-Template")

    override fun onInitialize() {
        logger.info("Fabric-Mod-Kotlin-Template initialized")
    }
}
