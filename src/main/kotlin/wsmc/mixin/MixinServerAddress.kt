package wsmc.mixin

import com.google.common.net.HostAndPort
import net.minecraft.client.multiplayer.resolver.ServerAddress
import org.spongepowered.asm.mixin.Debug
import org.spongepowered.asm.mixin.Final
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.Unique
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.Redirect
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import wsmc.IWebSocketServerAddress
import wsmc.WebSocketConnectionInfo

@Debug(export = true)
@Mixin(ServerAddress::class)
class MixinServerAddress : IWebSocketServerAddress {
    @field:Shadow
    @field:Final
    private lateinit var hostAndPort: HostAndPort

    @field:Unique
    private var connInfo: WebSocketConnectionInfo? = null

    @Inject(method = ["<init>(Ljava/lang/String;I)V"], at = [At("RETURN")], require = 1)
    private fun initWebSocketConnectionInfo(host: String, port: Int, callback: CallbackInfo) {
        val connInfo = WebSocketConnectionInfo.parseUri(host)
        if (connInfo != null) {
            this.connInfo = connInfo
        }
    }

    override fun setWsConnectionInfo(connInfo: WebSocketConnectionInfo?) {
        this.connInfo = connInfo
    }

    override fun getWsConnectionInfo(): WebSocketConnectionInfo? = connInfo

    override fun getRawHost(): String = hostAndPort.host

    @Inject(at = [At("HEAD")], method = ["toString"], require = 1, cancellable = true)
    private fun toStringCustom(callback: CallbackInfoReturnable<String>) {
        if (!this.isVanilla()) {
            callback.returnValue = this.connInfo.toString()
        }
    }

    @Inject(at = [At("HEAD")], method = ["equals"], require = 1, cancellable = true)
    private fun equalsCustom(`object`: Any, callback: CallbackInfoReturnable<Boolean>) {
        // Redirects the vanilla equals();
        if (this === `object`) callback.returnValue = true

        if (this.isVanilla()) {
            // We are vanilla
            if (`object` is IWebSocketServerAddress) {
                val ws = `object`
                // We are vanilla but input is modded
                if (!ws.isVanilla()) callback.returnValue = false
            }
            // Let vanilla code check for hostAndPort
            return
        } else {
            // We are modded
            if (`object` is IWebSocketServerAddress) {
                val ws = `object`
                if (ws.isVanilla()) callback.returnValue = false
                if (!this.connInfo!!.equalTo(ws.getWsConnectionInfo())) callback.returnValue = false
                // Let vanilla code check for hostAndPort
                return
            }
            callback.returnValue = false
        }
    }

    @Inject(at = [At("HEAD")], method = ["hashCode"], require = 1, cancellable = true)
    private fun hashCodeCustom(callback: CallbackInfoReturnable<Int>) {
        if (!this.isVanilla()) {
            // We are modded, cover hostname, port, and connInfo in the hash
            val hash = this.getWsConnectionInfo()!!.hashCode()
            callback.returnValue = hash
        }
    }

    private companion object {
        @JvmStatic
        @Redirect(
            method = ["<init>(Ljava/lang/String;I)V"],
            at = [At(
                value = "INVOKE",
                target = "Lcom/google/common/net/HostAndPort;fromParts(Ljava/lang/String;I)Lcom/google/common/net/HostAndPort;"
            )],
            require = 1
        )
        private fun parseWebSocketUriForConstructor(host: String, port: Int): HostAndPort {
            val connInfo = WebSocketConnectionInfo.parseUri(host)
            if (connInfo != null) {
                return HostAndPort.fromParts(connInfo.uri.host!!, connInfo.uri.port)
            }
            return HostAndPort.fromParts(host, port)
        }

        @JvmStatic
        @Inject(at = [At("HEAD")], method = ["parseString"], require = 1, cancellable = true)
        private fun parseString(uriString: String, callback: CallbackInfoReturnable<ServerAddress>) {
            val connInfo = WebSocketConnectionInfo.parseUri(uriString)
            if (connInfo != null) {
                val serverAddress = ServerAddress(connInfo.uri.host!!, connInfo.uri.port)
                ((serverAddress as Any) as IWebSocketServerAddress).setWsConnectionInfo(connInfo)
                callback.returnValue = serverAddress
            }
        }

        @JvmStatic
        @Inject(at = [At("HEAD")], method = ["isValidAddress"], require = 1, cancellable = true)
        private fun isValidAddress(uriString: String, callback: CallbackInfoReturnable<Boolean>) {
            if (WebSocketConnectionInfo.parseUri(uriString) != null) {
                callback.returnValue = true
            }
        }
    }
}
