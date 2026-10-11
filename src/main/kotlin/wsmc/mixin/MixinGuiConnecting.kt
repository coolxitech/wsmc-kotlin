package wsmc.mixin

import com.llamalad7.mixinextras.sugar.Local
import net.minecraft.client.multiplayer.GuiConnecting
import net.minecraft.client.multiplayer.ServerAddress
import net.minecraft.client.multiplayer.ServerData
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Unique
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import wsmc.IWsServerAddressHolder

@Mixin(GuiConnecting::class)
class MixinGuiConnecting : IWsServerAddressHolder {
    @Unique
    private var wsServerAddress: ServerAddress? = null

    override fun getWsServerAddress(): ServerAddress? = wsServerAddress

    override fun setWsServerAddress(address: ServerAddress?) {
        this.wsServerAddress = address
    }

    @Inject(
        method = ["<init>(Lnet/minecraft/client/gui/GuiScreen;Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/multiplayer/ServerData;)V"],
        at = [At("HEAD")],
        require = 1
    )
    fun init(callback: CallbackInfo, @Local(ordinal = 0, argsOnly = true) serverData: ServerData) {
        // 保存解析后的 ServerAddress（如果是 ws://，会带 WebSocketConnectionInfo）
        this.wsServerAddress = ServerAddress.fromString(serverData.serverIP)
    }
}
