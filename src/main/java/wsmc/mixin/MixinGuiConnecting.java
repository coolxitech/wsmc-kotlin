package wsmc.mixin;

import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerAddress;
import net.minecraft.client.multiplayer.ServerData;
import wsmc.IWsServerAddressHolder;

@Mixin(GuiConnecting.class)
public class MixinGuiConnecting implements IWsServerAddressHolder {
    @Unique
    private ServerAddress wsServerAddress;

    @Override
    public ServerAddress getWsServerAddress() {
        return this.wsServerAddress;
    }

    @Override
    public void setWsServerAddress(ServerAddress address) {
        this.wsServerAddress = address;
    }

    @Inject(
        method = "<init>(Lnet/minecraft/client/gui/GuiScreen;Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/multiplayer/ServerData;)V",
        at = @At("RETURN"),
        require = 1
    )
    private void init(CallbackInfo callback, @Local(ordinal = 0, argsOnly = true) ServerData serverData) {
        // 保存解析后的 ServerAddress（如果是 ws://，会带 WebSocketConnectionInfo）
        // 在 RETURN（super() 之后）注入，此时 this 已初始化，可以安全赋值实例字段。
        this.wsServerAddress = ServerAddress.fromString(serverData.serverIP);
    }
}
