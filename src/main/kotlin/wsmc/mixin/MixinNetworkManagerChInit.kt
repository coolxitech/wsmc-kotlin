package wsmc.mixin

import com.llamalad7.mixinextras.sugar.Local
import io.netty.channel.Channel
import net.minecraft.network.NetworkManager
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Unique
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import wsmc.IConnectionEx
import wsmc.client.WebSocketClientHandler

/**
 * This Mixin patches the [io.netty.channel.ChannelInitializer] inner class
 * in [net.minecraft.network.NetworkManager].
 * It injects additional handlers into the pipeline when the client connects to a server.
 */
@Mixin(targets = ["net.minecraft.network.NetworkManager\$5"])
class MixinNetworkManagerChInit {
    @Unique
    private var networkManager: NetworkManager? = null

    @Inject(method = ["<init>(Lnet/minecraft/network/NetworkManager;)V"], at = [At("RETURN")], require = 1)
    protected fun init(callback: CallbackInfo, @Local(ordinal = 0, argsOnly = true) networkManager: NetworkManager) {
        // These will remain constant
        this.networkManager = networkManager
    }

    @Inject(method = ["initChannel"], at = [At("RETURN")], require = 1)
    protected fun initChannel(channel: Channel, callback: CallbackInfo) {
        val nm = (this.networkManager as Any) as IConnectionEx
        val wsInfo = nm.getWsInfo()
        WebSocketClientHandler.hookPipeline(channel.pipeline(), wsInfo)
    }
}
