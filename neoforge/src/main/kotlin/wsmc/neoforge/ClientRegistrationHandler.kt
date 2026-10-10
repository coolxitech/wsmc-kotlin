package wsmc.neoforge

import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import wsmc.UnitTest
import wsmc.WSMC

@EventBusSubscriber(value = [Dist.CLIENT], modid = WSMC.MODID)
class ClientRegistrationHandler {
    companion object {
        @JvmStatic
        @SubscribeEvent
        fun onClientSetup(event: FMLClientSetupEvent) {
            if (!WSMC.debug()) return
            UnitTest.testParser()
        }
    }
}
