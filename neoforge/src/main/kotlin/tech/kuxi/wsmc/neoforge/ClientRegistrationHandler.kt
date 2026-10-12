package tech.kuxi.wsmc.neoforge

import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import tech.kuxi.wsmc.UnitTest
import tech.kuxi.wsmc.WSMC

@Mod.EventBusSubscriber(value = [Dist.CLIENT], modid = WSMC.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
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
