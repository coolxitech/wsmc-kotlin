package tech.kuxi.wsmc.mixin

import com.llamalad7.mixinextras.sugar.Local
import io.netty.channel.Channel
import net.minecraft.network.Connection
import org.spongepowered.asm.mixin.Debug
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Unique
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import tech.kuxi.wsmc.IConnectionEx
import tech.kuxi.wsmc.client.WebSocketClientHandler

/**
 * This Mixin patches the [io.netty.channel.ChannelInitializer] inner class
 * in [net.minecraft.network.Connection].
 * It injects additional handlers into the pipeline when the client connects to a server.
 */
@Debug(export = true)
@Mixin(targets = ["net.minecraft.network.Connection\$1"])
class MixinConnectionChInit {
    @field:Unique
    private var connection: Connection? = null

    @Inject(method = ["<init>"], at = [At("RETURN")], require = 1)
    protected fun init(callback: CallbackInfo, @Local(ordinal = 0, argsOnly = true) connection: Connection) {
        // These will remain constant
        this.connection = connection
    }

    @Inject(method = ["initChannel"], at = [At("RETURN")], require = 1)
    protected fun initChannel(channel: Channel, callback: CallbackInfo) {
        val con = (this.connection as Any) as IConnectionEx
        val wsInfo = con.getWsInfo()
        WebSocketClientHandler.hookPipeline(channel.pipeline(), wsInfo)
    }
}
