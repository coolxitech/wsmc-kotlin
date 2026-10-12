package tech.kuxi.wsmc

import net.minecraft.client.multiplayer.resolver.ServerAddress

object UnitTest {
    private fun check(a: Any?, b: Any?) {
        if (a == null) {
            if (b == null) return
        } else if (a == b) {
            return
        }
        throw RuntimeException()
    }

    fun testParser() {
        // The most simple WebSocket connection
        var serverAddr = WebSocketConnectionInfo.fromWsUri("ws://ip.ip.ip.ip/path")!!
        check(serverAddr.port, 80)
        check(IWebSocketServerAddress.from(serverAddr).getRawHost(), "ip.ip.ip.ip")
        check(IWebSocketServerAddress.from(serverAddr).getWsConnectionInfo()!!.sni, null)
        check(IWebSocketServerAddress.from(serverAddr).getWsConnectionInfo()!!.httpHostname, "ip.ip.ip.ip")

        // WebSocket connection with http hostname specified
        serverAddr = WebSocketConnectionInfo.fromWsUri("ws://host.com@ip.ip.ip.ip/path")!!
        check(serverAddr.port, 80)
        check(IWebSocketServerAddress.from(serverAddr).getRawHost(), "ip.ip.ip.ip")
        check(IWebSocketServerAddress.from(serverAddr).getWsConnectionInfo()!!.sni, null)
        check(IWebSocketServerAddress.from(serverAddr).getWsConnectionInfo()!!.httpHostname, "host.com")

        // WebSocket connection with empty http hostname specified
        serverAddr = WebSocketConnectionInfo.fromWsUri("ws://@ip.ip.ip.ip/path")!!
        check(serverAddr.port, 80)
        check(IWebSocketServerAddress.from(serverAddr).getRawHost(), "ip.ip.ip.ip")
        check(IWebSocketServerAddress.from(serverAddr).getWsConnectionInfo()!!.sni, null)
        check(IWebSocketServerAddress.from(serverAddr).getWsConnectionInfo()!!.httpHostname, "ip.ip.ip.ip")

        // The most simple WebSocket secured connection
        serverAddr = WebSocketConnectionInfo.fromWsUri("wss://ip.ip.ip.ip/path")!!
        check(serverAddr.port, 443)
        check(IWebSocketServerAddress.from(serverAddr).getRawHost(), "ip.ip.ip.ip")
        check(IWebSocketServerAddress.from(serverAddr).getWsConnectionInfo()!!.sni, "ip.ip.ip.ip")
        check(IWebSocketServerAddress.from(serverAddr).getWsConnectionInfo()!!.httpHostname, "ip.ip.ip.ip")

        // Specify port
        serverAddr = WebSocketConnectionInfo.fromWsUri("wss://ip.ip.ip.ip:11451/path")!!
        check(serverAddr.port, 11451)
        check(IWebSocketServerAddress.from(serverAddr).getRawHost(), "ip.ip.ip.ip")
        check(IWebSocketServerAddress.from(serverAddr).getWsConnectionInfo()!!.sni, "ip.ip.ip.ip")
        check(IWebSocketServerAddress.from(serverAddr).getWsConnectionInfo()!!.httpHostname, "ip.ip.ip.ip")

        // sni and http hostname are not set by the user, use the same as the given server IP/hostname
        serverAddr = WebSocketConnectionInfo.fromWsUri("wss://@ip.ip.ip.ip:11451/path")!!
        check(serverAddr.port, 11451)
        check(IWebSocketServerAddress.from(serverAddr).getRawHost(), "ip.ip.ip.ip")
        check(IWebSocketServerAddress.from(serverAddr).getWsConnectionInfo()!!.sni, "ip.ip.ip.ip")
        check(IWebSocketServerAddress.from(serverAddr).getWsConnectionInfo()!!.httpHostname, "ip.ip.ip.ip")

        // sni and http hostname are not set by the user, use the same as the given server IP/hostname
        serverAddr = WebSocketConnectionInfo.fromWsUri("wss://:@ip.ip.ip.ip:11451/path")!!
        check(serverAddr.port, 11451)
        check(IWebSocketServerAddress.from(serverAddr).getRawHost(), "ip.ip.ip.ip")
        check(IWebSocketServerAddress.from(serverAddr).getWsConnectionInfo()!!.sni, "ip.ip.ip.ip")
        check(IWebSocketServerAddress.from(serverAddr).getWsConnectionInfo()!!.httpHostname, "ip.ip.ip.ip")

        // Set sni and http hostname to the same value, set server IP/hostname separately
        serverAddr = WebSocketConnectionInfo.fromWsUri("wss://sni-host.com@ip.ip.ip.ip:11451/path")!!
        check(serverAddr.port, 11451)
        check(IWebSocketServerAddress.from(serverAddr).getRawHost(), "ip.ip.ip.ip")
        check(IWebSocketServerAddress.from(serverAddr).getWsConnectionInfo()!!.sni, "sni-host.com")
        check(IWebSocketServerAddress.from(serverAddr).getWsConnectionInfo()!!.httpHostname, "sni-host.com")

        // Set sni and http hostname differently, resolve server IP from host.com
        serverAddr = WebSocketConnectionInfo.fromWsUri("wss://sni.com:@host.com:11451/path")!!
        check(serverAddr.port, 11451)
        check(IWebSocketServerAddress.from(serverAddr).getRawHost(), "host.com")
        check(IWebSocketServerAddress.from(serverAddr).getWsConnectionInfo()!!.sni, "sni.com")
        check(IWebSocketServerAddress.from(serverAddr).getWsConnectionInfo()!!.httpHostname, "host.com")

        // Set sni and http hostname differently, resolve server IP from sni.com
        serverAddr = WebSocketConnectionInfo.fromWsUri("wss://:host.com@sni.com:11451/path")!!
        check(serverAddr.port, 11451)
        check(IWebSocketServerAddress.from(serverAddr).getRawHost(), "sni.com")
        check(IWebSocketServerAddress.from(serverAddr).getWsConnectionInfo()!!.sni, "sni.com")
        check(IWebSocketServerAddress.from(serverAddr).getWsConnectionInfo()!!.httpHostname, "host.com")

        // All set by the user separately
        serverAddr = WebSocketConnectionInfo.fromWsUri("wss://sni.com:host.com@ip.ip.ip.ip:11451/path")!!
        check(serverAddr.port, 11451)
        check(IWebSocketServerAddress.from(serverAddr).getRawHost(), "ip.ip.ip.ip")
        check(IWebSocketServerAddress.from(serverAddr).getWsConnectionInfo()!!.sni, "sni.com")
        check(IWebSocketServerAddress.from(serverAddr).getWsConnectionInfo()!!.httpHostname, "host.com")
    }
}
