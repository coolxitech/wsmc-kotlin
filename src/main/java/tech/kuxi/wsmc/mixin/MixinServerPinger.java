package tech.kuxi.wsmc.mixin;

import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.multiplayer.ServerAddress;
import net.minecraft.client.network.ServerPinger;
import tech.kuxi.wsmc.IConnectionEx;
import tech.kuxi.wsmc.IWebSocketServerAddress;

@Mixin(ServerPinger.class)
public class MixinServerPinger {
    @Inject(
        method = "ping",
        require = 1,
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/NetworkManager;createNetworkManagerAndConnect(Ljava/net/InetAddress;IZ)Lnet/minecraft/network/NetworkManager;"
        )
    )
    private void beforeCallConnect(CallbackInfo callback, @Local(ordinal = 0, argsOnly = false) ServerAddress serverAddress) {
        IWebSocketServerAddress wsAddress = IWebSocketServerAddress.from(serverAddress);
        IConnectionEx.connectToServerArg.push(wsAddress);
    }
}
