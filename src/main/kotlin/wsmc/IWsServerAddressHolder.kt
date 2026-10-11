package wsmc

import net.minecraft.client.multiplayer.ServerAddress

/**
 * 由 [wsmc.mixin.MixinGuiConnecting] 实现，用于把解析后的 [ServerAddress]
 * （可能带 [WebSocketConnectionInfo]）从 GuiConnecting 传递到其内部连接线程。
 */
interface IWsServerAddressHolder {
    fun getWsServerAddress(): ServerAddress?

    fun setWsServerAddress(address: ServerAddress?)
}
