package io.github.staakk.fwrap

/**
 * This class is responsible for providing [FunctionWrap] that will be executed on functions annotated with [Wrap].
 */
interface FunctionWrapProvider <T : FunctionWrap> {

    /**
     * Provides [FunctionWrap].
     */
    fun provide() : T
}