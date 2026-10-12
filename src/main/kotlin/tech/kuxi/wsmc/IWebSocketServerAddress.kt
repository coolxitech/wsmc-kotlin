package tech.kuxi.wsmc

import net.minecraft.client.multiplayer.resolver.ServerAddress

/**
 * This will be mixined into vanilla class [ServerAddress].
 * Assumes only [ServerAddress] implements this interface.
 *
 * You can cast between [ServerAddress] and this interface.
 */
interface IWebSocketServerAddress {
    fun setWsConnectionInfo(connInfo: WebSocketConnectionInfo?)

    fun getWsConnectionInfo(): WebSocketConnectionInfo?

    /**
     * Unlike [ServerAddress.getHost], this function:
     * @return the raw host name that may contain non-ascii characters.
     */
    fun getRawHost(): String

    fun isVanilla(): Boolean = getWsConnectionInfo() == null

    fun asServerAddress(): ServerAddress = (this as Any) as ServerAddress

    companion object {
        @JvmStatic
        fun from(serverAddress: ServerAddress): IWebSocketServerAddress =
            (serverAddress as Any) as IWebSocketServerAddress
    }
}
