package wsmc.mixin

import net.minecraft.client.multiplayer.ServerAddress
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.Unique
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import wsmc.IWebSocketServerAddress
import wsmc.WebSocketConnectionInfo

@Mixin(ServerAddress::class)
abstract class MixinServerAddress : IWebSocketServerAddress {
    @Shadow
    abstract fun getIP(): String

    @Unique
    private var connInfo: WebSocketConnectionInfo? = null

    override fun setWsConnectionInfo(connInfo: WebSocketConnectionInfo?) {
        this.connInfo = connInfo
    }

    override fun getWsConnectionInfo(): WebSocketConnectionInfo? = connInfo

    override fun getRawHost(): String = getIP()

    companion object {
        @JvmStatic
        @Inject(method = ["fromString"], at = [At("HEAD")], require = 1, cancellable = true)
        fun fromString(uriString: String, callback: CallbackInfoReturnable<ServerAddress>) {
            val serverAddress = WebSocketConnectionInfo.fromWsUri(uriString)
            if (serverAddress != null) {
                callback.returnValue = serverAddress
            }
        }
    }
}
