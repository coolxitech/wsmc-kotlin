package wsmc.mixin

import net.minecraft.client.multiplayer.GuiConnecting
import org.spongepowered.asm.mixin.Final
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import wsmc.IConnectionEx
import wsmc.IWebSocketServerAddress
import wsmc.IWsServerAddressHolder

@Mixin(targets = ["net.minecraft.client.multiplayer.GuiConnecting\$1"])
class MixinGuiConnectingThread {
    @Shadow
    @Final
    private lateinit var `this$0`: GuiConnecting

    @Inject(
        method = ["run"],
        require = 1,
        at = [At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/NetworkManager;createNetworkManagerAndConnect(Ljava/net/InetAddress;IZ)Lnet/minecraft/network/NetworkManager;"
        )]
    )
    fun beforeCallConnect(callback: CallbackInfo) {
        val holder = `this$0` as IWsServerAddressHolder
        val serverAddress = holder.getWsServerAddress() ?: return
        val wsAddress = IWebSocketServerAddress.from(serverAddress)
        if (!wsAddress.isVanilla()) {
            IConnectionEx.connectToServerArg.push(wsAddress)
        }
    }
}
