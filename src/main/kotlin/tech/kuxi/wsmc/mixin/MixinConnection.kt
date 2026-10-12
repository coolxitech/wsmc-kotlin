package tech.kuxi.wsmc.mixin

import com.llamalad7.mixinextras.sugar.Local
import net.minecraft.network.Connection
import org.spongepowered.asm.mixin.Debug
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Unique
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import tech.kuxi.wsmc.IConnectionEx
import tech.kuxi.wsmc.IWebSocketServerAddress

@Debug(export = true)
@Mixin(Connection::class)
class MixinConnection : IConnectionEx {
    @field:Unique
    private var wsInfo: IWebSocketServerAddress? = null

    override fun getWsInfo(): IWebSocketServerAddress? = wsInfo

    override fun setWsInfo(wsInfo: IWebSocketServerAddress?) {
        this.wsInfo = wsInfo
    }

    private companion object {
        /*
         * Prior to the invocation of connectToServer(), call [ArgHolder.push]
         * to get the ServerAddress-sensitive(IWebSocketServerAddress) version of connectToServer().
         */
        @JvmStatic
        @Inject(
            method = ["connectToServer"],
            require = 1,
            at = [At(
                value = "INVOKE",
                target = "Lnet/minecraft/network/Connection;connect(Ljava/net/InetSocketAddress;Lnet/minecraft/server/network/EventLoopGroupHolder;Lnet/minecraft/network/Connection;)Lio/netty/channel/ChannelFuture;"
            )]
        )
        private fun beforeCallConnect(
            callback: CallbackInfoReturnable<Connection>,
            @Local(ordinal = 0, argsOnly = false) connection: Connection
        ) {
            val wsAddress = IConnectionEx.connectToServerArg.pop()
            val con = (connection as Any) as IConnectionEx
            con.setWsInfo(wsAddress)
        }
    }
}
