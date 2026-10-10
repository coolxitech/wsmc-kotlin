package wsmc.forge

import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.eventbus.api.listener.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import wsmc.UnitTest
import wsmc.WSMC

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
