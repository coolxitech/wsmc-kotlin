package tech.kuxi.wsmc.mixin;

import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.netty.channel.Channel;
import net.minecraft.network.NetworkManager;
import tech.kuxi.wsmc.IConnectionEx;
import tech.kuxi.wsmc.client.WebSocketClientHandler;

/**
 * This Mixin patches the {@link io.netty.channel.ChannelInitializer} inner class
 * in {@link net.minecraft.network.NetworkManager}.
 * It injects additional handlers into the pipeline when the client connects to a server.
 */
@Mixin(targets = "net.minecraft.network.NetworkManager$5")
public class MixinNetworkManagerChInit {
    @Unique
    private NetworkManager networkManager;

    @Inject(method = "<init>(Lnet/minecraft/network/NetworkManager;)V", at = @At("RETURN"), require = 1)
    protected void init(CallbackInfo callback, @Local(ordinal = 0, argsOnly = true) NetworkManager networkManager) {
        // These will remain constant
        this.networkManager = networkManager;
    }

    @Inject(method = "initChannel", at = @At("RETURN"), require = 1)
    protected void initChannel(Channel channel, CallbackInfo callback) {
        IConnectionEx nm = (IConnectionEx) (Object) this.networkManager;
        WebSocketClientHandler.hookPipeline(channel.pipeline(), nm.getWsInfo());
    }
}
