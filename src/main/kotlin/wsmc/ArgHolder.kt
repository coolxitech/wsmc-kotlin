package wsmc

class ArgHolder<T> private constructor(private val nullable: Boolean) {
    private val wsAddress = ThreadLocal.withInitial<T?> { null }

    fun push(value: T?) {
        if (wsAddress.get() != null) {
            throw RuntimeException("Previous WebsocketUriHolder.wsAddress has not been used!")
        }
        wsAddress.set(value)
    }

    /**
     * Get the value
     * @return the value set previously
     */
    fun peek(): T? {
        val ret = wsAddress.get()
        if (ret == null && !nullable) {
            throw RuntimeException("WebsocketUriHolder.wsAddress is not available!")
        }
        return ret
    }

    /**
     * Get the value and then reset the context.
     * @return the value set previously
     */
    fun pop(): T? {
        val ret = peek()
        wsAddress.set(null)
        return ret
    }

    companion object {
        @JvmStatic
        fun <T> nullable(): ArgHolder<T> = ArgHolder(true)

        @JvmStatic
        fun <T> nonnull(): ArgHolder<T> = ArgHolder(false)
    }
}
