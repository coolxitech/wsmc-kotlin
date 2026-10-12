package tech.kuxi.wsmc.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.multiplayer.ServerAddress;
import tech.kuxi.wsmc.IWebSocketServerAddress;
import tech.kuxi.wsmc.WebSocketConnectionInfo;

@Mixin(ServerAddress.class)
public abstract class MixinServerAddress implements IWebSocketServerAddress {
    @Shadow
    public abstract String getIP();

    @Unique
    private WebSocketConnectionInfo connInfo;

    @Override
    public void setWsConnectionInfo(WebSocketConnectionInfo connInfo) {
        this.connInfo = connInfo;
    }

    @Override
    public WebSocketConnectionInfo getWsConnectionInfo() {
        return this.connInfo;
    }

    @Override
    public String getRawHost() {
        return this.getIP();
    }

    @Inject(method = "fromString", at = @At("HEAD"), require = 1, cancellable = true)
    private static void fromString(String uriString, CallbackInfoReturnable<ServerAddress> callback) {
        ServerAddress serverAddress = WebSocketConnectionInfo.fromWsUri(uriString);
        if (serverAddress != null) {
            callback.setReturnValue(serverAddress);
            callback.cancel();
        }
    }
}
