package tech.kuxi.wsmc

object WSMC {
    const val MODID = "wsmc"

    private val isDebug = System.getProperty("wsmc.debug", "false").equals("true", ignoreCase = true)

    @JvmField
    val dumpBytes = System.getProperty("wsmc.dumpBytes", "false").equals("true", ignoreCase = true)

    @JvmStatic
    fun debug(msg: String) {
        if (!isDebug) return
        println("[WSMC D] $msg")
    }

    @JvmStatic
    fun info(msg: String) {
        println("[WSMC I] $msg")
    }

    @JvmStatic
    fun debug(): Boolean = isDebug
}
