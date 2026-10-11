package wsmc.mixin

import com.llamalad7.mixinextras.sugar.Local
import net.minecraft.client.multiplayer.ServerAddress
import net.minecraft.client.network.ServerPinger
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import wsmc.IConnectionEx
import wsmc.IWebSocketServerAddress

@Mixin(ServerPinger::class)
class MixinServerPinger {
    @Inject(
        method = ["ping"],
        require = 1,
        at = [At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/NetworkManager;createNetworkManagerAndConnect(Ljava/net/InetAddress;IZ)Lnet/minecraft/network/NetworkManager;"
        )]
    )
    fun beforeCallConnect(
        callback: CallbackInfo,
        @Local(ordinal = 0, argsOnly = false) serverAddress: ServerAddress
    ) {
        val wsAddress = IWebSocketServerAddress.from(serverAddress)
        IConnectionEx.connectToServerArg.push(wsAddress)
    }
}
