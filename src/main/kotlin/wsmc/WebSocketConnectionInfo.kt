package wsmc

import com.google.common.base.Objects
import net.minecraft.client.multiplayer.resolver.ServerAddress
import java.net.IDN
import java.net.URI
import java.net.URISyntaxException

/**
 * This class defines additional information added to the vanilla [ServerAddress].
 *
 * You are not supposed to compare two [WebSocketConnectionInfo] instances directly.
 *
 * Compare vanilla [ServerAddress] instances instead.
 *
 * Both vanilla [ServerAddress] and this class are immutable.
 */
class WebSocketConnectionInfo private constructor(
    val owner: ServerAddress,
    val uri: URI,
    val sni: String?,
    val httpHostname: String?
) {
    /**
     * Compare URI components, sni and httpHostname.
     *
     * Does not check the owner ([ServerAddress]).
     *
     * Vanilla ServerAddress checks Host and port.
     */
    fun equalTo(that: WebSocketConnectionInfo?): Boolean {
        if (that == null) return false
        if (this.uri != that.uri) return false
        if (this.sni != that.sni) return false
        if (this.httpHostname != that.httpHostname) return false
        return true
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WebSocketConnectionInfo) return false

        // Check vanilla fields
        if (IWebSocketServerAddress.from(this.owner).getRawHost() != IWebSocketServerAddress.from(other.owner).getRawHost())
            return false

        if (this.owner.port != other.owner.port)
            return false

        return this.equalTo(other)
    }

    override fun hashCode(): Int = Objects.hashCode(
        this.owner.port,
        IWebSocketServerAddress.from(this.owner).getRawHost(),
        this.uri,
        this.sni,
        this.httpHostname
    )

    override fun toString(): String =
        this.uri.toString() + "\n" +
            "TLS SNI: " + this.sni + "\n" +
            "HTTP Hostname: " + this.httpHostname

    companion object {
        private fun splitUserInfo(userInfo: String?): List<String> {
            if (userInfo == null) return emptyList()
            // Split the UserInfo string, preserve empty strings
            return userInfo.split(":", limit = -1)
        }

        /**
         * syntax: (wss|ws)://sni.com:host.com@ip.ip.ip.ip[:port][/path]
         * @return null if uriString is not a valid WebSocket Uri (including vanilla TCP).
         */
        @JvmStatic
        fun fromWsUri(uriString: String): ServerAddress? {
            try {
                var uri = URI(uriString)

                val scheme = uri.scheme
                val hostname = uri.host

                if (hostname == null) return null

                IDN.toASCII(hostname)

                if (scheme == null) return null

                if (!scheme.equals("ws", ignoreCase = true) && !scheme.equals("wss", ignoreCase = true)) {
                    return null
                }

                var port = uri.port
                if (port < 0 || port > 65535) {
                    // Default port
                    port = when {
                        scheme.equals("ws", ignoreCase = true) -> 80
                        scheme.equals("wss", ignoreCase = true) -> 443
                        else -> 25565
                    }
                }

                var path = uri.path
                if (path == null) {
                    path = "/"
                }

                var sni: String? = null
                var hostnameInHeader: String? = null

                if ("wss".equals(scheme, ignoreCase = true)) {
                    sni = hostname
                    hostnameInHeader = hostname

                    val splitted = splitUserInfo(uri.userInfo)
                    if (splitted.isNotEmpty()) {
                        var s = splitted[0]
                        var h = if (splitted.size == 1) s else splitted[1]

                        if (h.isEmpty()) {
                            h = hostname
                            if (s.isEmpty()) s = hostname
                        } else if (s.isEmpty()) {
                            s = hostname
                        }

                        sni = s
                        hostnameInHeader = h
                    }
                } else if ("ws".equals(scheme, ignoreCase = true)) {
                    hostnameInHeader = hostname

                    val splitted = splitUserInfo(uri.userInfo)
                    if (splitted.isNotEmpty() && !splitted[0].isBlank()) {
                        hostnameInHeader = splitted[0]
                    }
                }

                uri = URI(scheme, null, hostname, port, path, uri.query, uri.fragment)

                val result = ServerAddress(hostname, port)
                val connInfo = WebSocketConnectionInfo(result, uri, sni, hostnameInHeader)
                (result as Any as IWebSocketServerAddress).setWsConnectionInfo(connInfo)
                return result
            } catch (e: URISyntaxException) {
            }

            return null
        }
    }
}
