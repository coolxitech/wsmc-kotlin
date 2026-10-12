package tech.kuxi.wsmc.mixin;

import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.network.NetworkManager;
import tech.kuxi.wsmc.IConnectionEx;
import tech.kuxi.wsmc.IWebSocketServerAddress;

@Mixin(NetworkManager.class)
public class MixinNetworkManager implements IConnectionEx {
    @Unique
    private IWebSocketServerAddress wsInfo;

    @Override
    public IWebSocketServerAddress getWsInfo() {
        return this.wsInfo;
    }

    @Override
    public void setWsInfo(IWebSocketServerAddress wsInfo) {
        this.wsInfo = wsInfo;
    }

    /*
     * 在调用 createNetworkManagerAndConnect() 之前，先调用 [IConnectionEx.connectToServerArg.pop]
     * 以便拿到对 ServerAddress(IWebSocketServerAddress) 敏感的 connectToServer 版本。
     */
    @Inject(
        method = "createNetworkManagerAndConnect",
        require = 1,
        at = @At(
            value = "NEW",
            target = "()Lio/netty/bootstrap/Bootstrap;"
        )
    )
    private static void beforeCallConnect(
        CallbackInfoReturnable<NetworkManager> callback,
        @Local(ordinal = 0, argsOnly = false) NetworkManager networkManager
    ) {
        IWebSocketServerAddress wsAddress = IConnectionEx.connectToServerArg.pop();
        ((IConnectionEx) (Object) networkManager).setWsInfo(wsAddress);
    }
}
