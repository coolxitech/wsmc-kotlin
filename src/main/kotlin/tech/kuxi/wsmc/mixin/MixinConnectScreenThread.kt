package tech.kuxi.wsmc.mixin

import com.llamalad7.mixinextras.sugar.Local
import net.minecraft.client.multiplayer.resolver.ServerAddress
import org.spongepowered.asm.mixin.Debug
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Unique
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import tech.kuxi.wsmc.IConnectionEx
import tech.kuxi.wsmc.IWebSocketServerAddress

@Debug(export = true)
@Mixin(targets = ["net.minecraft.client.gui.screens.ConnectScreen\$1"])
class MixinConnectScreenThread {
    @field:Unique
    private var serverAddress: ServerAddress? = null

    @Inject(method = ["<init>"], at = [At("RETURN")], require = 1)
    protected fun init(callback: CallbackInfo, @Local(ordinal = 0, argsOnly = true) serverAddress: ServerAddress) {
        // This will remain constant
        this.serverAddress = serverAddress
    }

    @Inject(
        method = ["run"],
        require = 1,
        at = [At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/Connection;connectToServer(Ljava/net/InetSocketAddress;Z)Lnet/minecraft/network/Connection;"
        )]
    )
    fun beforeCallConnect(callback: CallbackInfo) {
        val wsAddress = IWebSocketServerAddress.from(serverAddress!!)
        IConnectionEx.connectToServerArg.push(wsAddress)
    }
}
