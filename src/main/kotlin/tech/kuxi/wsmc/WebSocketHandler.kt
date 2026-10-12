package tech.kuxi.wsmc

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame
import io.netty.handler.codec.http.websocketx.WebSocketFrame

abstract class WebSocketHandler(
    val inboundPrefix: String,
    val outboundPrefix: String
) : ChannelDuplexHandler() {

    protected abstract fun sendWsFrame(ctx: ChannelHandlerContext, frame: WebSocketFrame, promise: ChannelPromise)

    final override fun write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise) {
        if (msg is ByteBuf) {
            val byteBuf = msg
            if (WSMC.debug()) {
                WSMC.debug(this.outboundPrefix + " (" + byteBuf.readableBytes() + "):")
                WebSocketHandler.dumpByteArray(byteBuf)
            }
            sendWsFrame(ctx, BinaryWebSocketFrame(byteBuf), promise)
        } else {
            // DefaultFullHttpResponse
            WSMC.debug(this.outboundPrefix + " Passthrough: " + msg.javaClass.name)
            ctx.write(msg, promise)
        }
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (msg is WebSocketFrame) {
            if (msg is BinaryWebSocketFrame) {
                val content = msg.content()
                if (WSMC.debug()) {
                    WSMC.debug(this.inboundPrefix + " (" + content.readableBytes() + "):")
                    WebSocketHandler.dumpByteArray(content)
                }
                ctx.fireChannelRead(content)
            } else if (msg is CloseWebSocketFrame) {
                WSMC.debug("CloseWebSocketFrame (" + msg.statusCode() + ") received : " + msg.reasonText())
            } else {
                WSMC.debug("Unsupported WebSocketFrame: " + msg.javaClass.name)
            }
        }
    }

    companion object {
        @JvmStatic
        fun dumpByteArray(byteArray: ByteBuf) {
            if (!WSMC.dumpBytes) return

            val maxBytesPerLine = 32
            val totalBytes = byteArray.readableBytes()
            byteArray.markReaderIndex()

            var i = 0
            while (i < totalBytes) {
                val remainingBytes = minOf(maxBytesPerLine, totalBytes - i)
                val line = StringBuilder()
                for (j in 0 until remainingBytes) {
                    val currentByte = byteArray.readByte()
                    line.append(String.format("%02X ", currentByte))
                }
                println(line.toString().trim())
                i += maxBytesPerLine
            }
            byteArray.resetReaderIndex()
        }
    }
}
