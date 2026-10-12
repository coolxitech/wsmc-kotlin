package tech.kuxi.wsmc.forge

import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
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
