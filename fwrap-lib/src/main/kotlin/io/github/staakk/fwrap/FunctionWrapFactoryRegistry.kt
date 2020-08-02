package io.github.staakk.fwrap

@Suppress("unused") // Public API.
object FunctionWrapFactoryRegistry {
    private val wraps = mutableMapOf<String, FunctionWrapFactory<*>>()

    fun registerFactory(id : String, factory: FunctionWrapFactory<*>) = wraps.put(id, factory)

    fun get(id: String): FunctionWrap = wraps[id]!!.create()
}