package tech.kuxi.wsmc.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerAddress;
import tech.kuxi.wsmc.IConnectionEx;
import tech.kuxi.wsmc.IWebSocketServerAddress;
import tech.kuxi.wsmc.IWsServerAddressHolder;

@Mixin(targets = "net.minecraft.client.multiplayer.GuiConnecting$1")
public class MixinGuiConnectingThread {
    @Shadow
    @Final
    private GuiConnecting this$0;

    @Inject(
        method = "run",
        require = 1,
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/NetworkManager;createNetworkManagerAndConnect(Ljava/net/InetAddress;IZ)Lnet/minecraft/network/NetworkManager;"
        )
    )
    private void beforeCallConnect(CallbackInfo callback) {
        IWsServerAddressHolder holder = (IWsServerAddressHolder) (Object) this.this$0;
        ServerAddress serverAddress = holder.getWsServerAddress();
        if (serverAddress == null) return;

        IWebSocketServerAddress wsAddress = IWebSocketServerAddress.from(serverAddress);
        if (!wsAddress.isVanilla()) {
            IConnectionEx.connectToServerArg.push(wsAddress);
        }
    }
}
