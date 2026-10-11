package wsmc.mixin

import com.llamalad7.mixinextras.sugar.Local
import net.minecraft.network.NetworkManager
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Unique
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import wsmc.IConnectionEx
import wsmc.IWebSocketServerAddress

@Mixin(NetworkManager::class)
class MixinNetworkManager : IConnectionEx {
    @Unique
    private var wsInfo: IWebSocketServerAddress? = null

    override fun getWsInfo(): IWebSocketServerAddress? = wsInfo

    override fun setWsInfo(wsInfo: IWebSocketServerAddress?) {
        this.wsInfo = wsInfo
    }

    companion object {
        /*
         * 在调用 createNetworkManagerAndConnect() 之前，先调用 [IConnectionEx.connectToServerArg.push]
         * 以便拿到对 ServerAddress(IWebSocketServerAddress) 敏感的 connectToServer 版本。
         */
        @JvmStatic
        @Inject(
            method = ["createNetworkManagerAndConnect"],
            require = 1,
            at = [At(
                value = "NEW",
                target = "()Lio/netty/bootstrap/Bootstrap;"
            )]
        )
        fun beforeCallConnect(
            callback: CallbackInfoReturnable<NetworkManager>,
            @Local(ordinal = 0, argsOnly = false) networkManager: NetworkManager
        ) {
            val wsAddress = IConnectionEx.connectToServerArg.pop()
            val nm = (networkManager as Any) as IConnectionEx
            nm.setWsInfo(wsAddress)
        }
    }
}
