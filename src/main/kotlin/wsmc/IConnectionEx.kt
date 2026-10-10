package wsmc

interface IConnectionEx {
    fun getWsInfo(): IWebSocketServerAddress?

    fun setWsInfo(wsInfo: IWebSocketServerAddress?)

    companion object {
        @JvmField
        val connectToServerArg: ArgHolder<IWebSocketServerAddress> = ArgHolder.nullable()
    }
}
