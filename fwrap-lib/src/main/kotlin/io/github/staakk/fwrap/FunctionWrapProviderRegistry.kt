package io.github.staakk.fwrap

import java.lang.IllegalStateException

/**
 * This class is responsible for keeping registry of providers for [Wrap] annotation.
 */
@Suppress("unused") // Public API.
object FunctionWrapProviderRegistry {
    private val wraps = mutableMapOf<String, () -> FunctionWrap>()

    /**
     * Registers [FunctionWrap]s provider for [Wrap] annotated methods.
     *
     * @param id Unique identifier for registered provider. If provider gets registered with
     * already existing id old provider will be overridden.
     * @param provider Provider to be registered.
     */
    fun registerProvider(id: String, provider: () -> FunctionWrap) {
        wraps[id] = provider
    }

    /**
     * Registers [FunctionWrap]s provider for [Wrap] annotated methods..
     *
     * @param id Unique identifier for registered provider. If provider gets registered with
     * already existing id old provider will be overridden.
     * @param callBefore Function to be called before [Wrap] annotated function execution.
     * @param callAfter Function to be called after [Wrap] annotated function execution.
     */
    fun registerProvider(id: String, callBefore: (FunctionInvocation) -> Unit, callAfter: (Any?) -> Unit) {
        wraps[id] = {
            object : FunctionWrap {
                override fun before(invocation: FunctionInvocation) = callBefore(invocation)
                override fun after(returnValue: Any?) = callAfter(returnValue)
            }
        }
    }

    /**
     * Removes provider with [id] from this registry.
     *
     * @param id Factory unique identifier.
     */
    fun removeProvider(id: String) {
        wraps.remove(id)
    }

    /**
     * Get new [FunctionWrap] created by provider with [id].
     *
     * @param id Factory id.
     * @return [FunctionWrap] From provider identified by [id]
     * @throws IllegalStateException When provider with [id] was not registered.
     */
    fun get(id: String): FunctionWrap = wraps[id]?.let { it() }
            ?: throw IllegalStateException("Provider with id=${id} doesn't exits.")
}