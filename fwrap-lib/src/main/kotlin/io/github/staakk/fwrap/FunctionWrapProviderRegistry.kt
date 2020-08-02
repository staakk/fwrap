package io.github.staakk.fwrap

import java.lang.IllegalStateException

/**
 * This class is responsible for keeping registry of providers for [Wrap] annotation.
 */
@Suppress("unused") // Public API.
object FunctionWrapProviderRegistry {
    private val wraps = mutableMapOf<String, FunctionWrapProvider>()

    /**
     * Register provider to be used for providing [FunctionWrap]s for methods annotated with [Wrap] annotation.
     *
     * @param id Unique identifier for registered provider. If provider gets registered with
     * already existing id old provider will be overridden.
     * @param provider Factory to be registered.
     * @return Overridden provider, or `null` if there was no provider with given [id].
     */
    fun registerProvider(id : String, provider: FunctionWrapProvider): FunctionWrapProvider? = wraps.put(id, provider)

    /**
     * Removes provider with [id] from this registry.
     *
     * @param id Factory unique identifier.
     * @return Removed provider or `null` if it didn't exist.
     */
    fun removeProvider(id: String): FunctionWrapProvider? = wraps.remove(id)

    /**
     * Get new [FunctionWrap] created by provider with [id].
     *
     * @param id Factory id.
     * @return [FunctionWrap] From provider identified by [id]
     * @throws IllegalStateException When provider with [id] was not registered.
     */
    fun get(id: String): FunctionWrap = wraps[id]?.provide()
            ?: throw IllegalStateException("Provider with id=${id} doesn't exits.")
}