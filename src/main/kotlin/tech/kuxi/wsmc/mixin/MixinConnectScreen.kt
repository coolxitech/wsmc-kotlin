package tech.kuxi.wsmc.mixin

import com.llamalad7.mixinextras.sugar.Local
import net.minecraft.client.gui.screens.ConnectScreen
import net.minecraft.client.multiplayer.resolver.ServerAddress
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.spongepowered.asm.mixin.Debug
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Unique
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import tech.kuxi.wsmc.IWebSocketServerAddress

@Debug(export = true)
@Mixin(ConnectScreen::class)
class MixinConnectScreen {
    @Inject(at = [At("HEAD")], method = ["connect"], require = 1, cancellable = true)
    private fun connect(
        callback: CallbackInfo,
        @Local(ordinal = 0, argsOnly = true) serverAddress: ServerAddress
    ) {
        if (!IWebSocketServerAddress.from(serverAddress).isVanilla()) {
            LOGGER.info("Connecting to Websocket server: " + serverAddress.toString())
        }
    }

    private companion object {
        @field:Unique
        private val LOGGER: Logger = LoggerFactory.getLogger("wsmc")
    }
}
