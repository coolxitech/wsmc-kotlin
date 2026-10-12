package tech.kuxi.wsmc.client

import io.netty.buffer.ByteBufAllocator
import io.netty.channel.Channel
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPipeline
import io.netty.channel.ChannelPromise
import io.netty.handler.codec.http.DefaultHttpHeaders
import io.netty.handler.codec.http.FullHttpResponse
import io.netty.handler.codec.http.HttpClientCodec
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory
import io.netty.handler.codec.http.websocketx.WebSocketFrame
import io.netty.handler.codec.http.websocketx.WebSocketHandshakeException
import io.netty.handler.codec.http.websocketx.WebSocketVersion
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler
import io.netty.handler.ssl.SslContext
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.SslHandler
import io.netty.handler.ssl.util.InsecureTrustManagerFactory
import io.netty.util.CharsetUtil
import tech.kuxi.wsmc.IWebSocketServerAddress
import tech.kuxi.wsmc.WSMC
import tech.kuxi.wsmc.WebSocketConnectionInfo
import tech.kuxi.wsmc.WebSocketHandler
import java.io.File
import java.io.FileInputStream
import java.net.URI
import java.security.KeyStore
import java.util.Collections
import java.util.Properties
import javax.net.ssl.SNIHostName
import javax.net.ssl.SSLEngine
import javax.net.ssl.SSLException
import javax.net.ssl.SSLParameters
import javax.net.ssl.KeyManagerFactory

class WebSocketClientHandler(uri: URI, httpHostname: String) : WebSocketHandler("S->C", "C->S") {
    private val handshaker: WebSocketClientHandshaker
    private lateinit var handshakeFuture: ChannelPromise

    init {
        var maxFramePayloadLength = 65536
        try {
            maxFramePayloadLength = WebSocketClientHandler.maxFramePayloadLength.toInt()
        } catch (e: Exception) {
            WSMC.debug("Unable to parse maxFramePayloadLength, value: " + WebSocketClientHandler.maxFramePayloadLength)
        }

        val headers = DefaultHttpHeaders()
        headers.set("Host", httpHostname)

        // Connect with V13 (RFC 6455 aka HyBi-17).
        this.handshaker = WebSocketClientHandshakerFactory.newHandshaker(
            uri, WebSocketVersion.V13, null, true, headers, maxFramePayloadLength
        )
    }

    override fun handlerAdded(ctx: ChannelHandlerContext) {
        super.handlerAdded(ctx)
        this.handshakeFuture = ctx.newPromise()
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        super.channelActive(ctx)
        handshaker.handshake(ctx.channel())
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        super.channelInactive(ctx)
        WSMC.debug(this.inboundPrefix + " WebSocket Client disconnected!")
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        super.exceptionCaught(ctx, cause)
        cause.printStackTrace()
        if (!handshakeFuture.isDone) {
            handshakeFuture.setFailure(cause)
        }
        ctx.close()
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val ch: Channel = ctx.channel()
        if (!handshaker.isHandshakeComplete) {
            try {
                handshaker.finishHandshake(ch, msg as FullHttpResponse)
                WSMC.debug(this.inboundPrefix + " WebSocket Client connected!")
                handshakeFuture.setSuccess()
            } catch (e: WebSocketHandshakeException) {
                WSMC.debug(this.inboundPrefix + " WebSocket Client failed to connect")
                handshakeFuture.setFailure(e)
            }
            return
        }

        if (msg is FullHttpResponse) {
            val response: FullHttpResponse = msg
            throw IllegalStateException(
                "Unexpected FullHttpResponse (getStatus=" + response.status()
                    + ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')'
            )
        }

        super.channelRead(ctx, msg)
    }

    override fun sendWsFrame(ctx: ChannelHandlerContext, frame: WebSocketFrame, promise: ChannelPromise) {
        if (handshakeFuture.isSuccess) {
            ctx.write(frame, promise)
        } else {
            handshakeFuture.addListener(ChannelFutureListener {
                if (handshakeFuture.isSuccess) {
                    ctx.write(frame, promise)
                }
            })
        }
    }

    companion object {
        /**
         * This will set your maximum allowable frame payload length.
         * Setting this value for big modpack.
         */
        @JvmField
        val maxFramePayloadLength: String = System.getProperty("wsmc.maxFramePayloadLength", "65536")

        private const val CONFIG_DIR = "config"
        private const val CONFIG_FILE = "wsmc.properties"
        private const val DEFAULT_KEY_STORE = "config/wsmc-client.p12"

        /**
         * Client TLS certificate (mutual TLS) configuration, loaded from `config/wsmc.properties`.
         */
        private val sslConfig: Properties = loadConfig()

        private val keyStoreExplicitlySet: Boolean = sslConfig.containsKey("keyStore")

        /**
         * Path to a client identity key store (PKCS12/JKS) containing the client
         * certificate chain and private key, presented to the server during the
         * TLS handshake (mutual TLS). Defaults to [DEFAULT_KEY_STORE]; when that
         * file does not exist, no client certificate is sent.
         */
        @JvmField
        val keyStore: String? = sslConfig.getProperty("keyStore", DEFAULT_KEY_STORE)

        /**
         * Password for [keyStore]. Also used as the private key password when [keyPassword] is unset.
         */
        @JvmField
        val keyStorePassword: String? = sslConfig.getProperty("keyStorePassword")

        /**
         * Private key password, used only when it differs from [keyStorePassword].
         */
        @JvmField
        val keyPassword: String? = sslConfig.getProperty("keyPassword")

        /**
         * Key store type for [keyStore] (JKS / PKCS12). Inferred from the file extension when unset.
         */
        @JvmField
        val keyStoreType: String? = sslConfig.getProperty("keyStoreType")

        private fun loadConfig(): Properties {
            val properties = Properties()
            val configFile = File(CONFIG_DIR, CONFIG_FILE)
            if (configFile.isFile) {
                try {
                    FileInputStream(configFile).use { properties.load(it) }
                } catch (e: Exception) {
                    WSMC.info("Failed to load " + configFile.absolutePath)
                    e.printStackTrace()
                }
            }
            return properties
        }

        @JvmStatic
        fun hookPipeline(pipeline: ChannelPipeline, wsInfo: IWebSocketServerAddress?) {
            // Do not perform WebSocket handshake for vanilla TCP Minecraft
            if (wsInfo != null && !wsInfo.isVanilla()) {
                val connInfo: WebSocketConnectionInfo = wsInfo.getWsConnectionInfo()!!
                val handler = WebSocketClientHandler(connInfo.uri, connInfo.httpHostname!!)
                WSMC.info("Connecting to WebSocket Server:\n" + connInfo.toString())

                pipeline.addAfter("timeout", "WsmcHttpClient", HttpClientCodec())
                pipeline.addAfter("WsmcHttpClient", "WsmcHttpAggregator", HttpObjectAggregator(8192 * 4))
                pipeline.addAfter("WsmcHttpAggregator", "WsmcCompressionHandler", WebSocketClientCompressionHandler.INSTANCE)
                pipeline.addAfter("WsmcCompressionHandler", "WsmcWebSocketClientHandler", handler)

                if ("wss".equals(connInfo.uri.scheme, ignoreCase = true)) {
                    try {
                        val sslCtx: SslContext = buildSslContext()

                        // SSL Parameters to set SNI TLS Extension
                        val sslParameters = SSLParameters()
                        sslParameters.serverNames = Collections.singletonList(SNIHostName(connInfo.sni!!))

                        // SSLEngine with SSL Parameters for SNI
                        val sslEngine: SSLEngine = sslCtx.newEngine(ByteBufAllocator.DEFAULT)
                        sslEngine.sslParameters = sslParameters

                        // SSL Handler
                        val sslHandler = SslHandler(sslEngine)

                        pipeline.addAfter("timeout", "WsmcSslHandler", sslHandler)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        /**
         * Build the client [SslContext].
         *
         * Server certificates are never verified (every certificate is trusted),
         * matching the original behaviour. When [keyStore] is set, the client
         * certificate chain and private key are presented to the server during
         * the TLS handshake (mutual TLS).
         */
        @JvmStatic
        fun buildSslContext(): SslContext {
            val builder = SslContextBuilder.forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)

            val keyStorePath = keyStore
            if (keyStorePath.isNullOrBlank()) {
                return builder.build()
            }

            val keyStoreFile = File(keyStorePath)
            if (!keyStoreFile.isFile) {
                if (keyStoreExplicitlySet) {
                    throw SSLException("Key store file not found: $keyStorePath")
                }
                // Default key store does not exist; connect without a client certificate.
                return builder.build()
            }

            val resolvedKeyStoreType = keyStoreType?.takeIf { it.isNotBlank() }
                ?: inferKeyStoreType(keyStoreFile)
            val identityStore = KeyStore.getInstance(resolvedKeyStoreType)
            FileInputStream(keyStoreFile).use { input ->
                identityStore.load(input, keyStorePassword?.toCharArray())
            }

            val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
            keyManagerFactory.init(identityStore, keyPassword?.toCharArray() ?: keyStorePassword?.toCharArray())

            return builder.keyManager(keyManagerFactory).build()
        }

        private fun inferKeyStoreType(file: File): String =
            when (file.extension.lowercase()) {
                "jks" -> "JKS"
                "p12", "pfx" -> "PKCS12"
                else -> KeyStore.getDefaultType()
            }
    }
}
