package io.github.staakk.fwrap

/**
 * This class is responsible for providing [FunctionWrap] that will be executed on functions annotated with [Wrap].
 */
@Suppress("unused") // Public API.
interface FunctionWrapProvider {

    /**
     * Provides [FunctionWrap].
     */
    fun provide() : FunctionWrap

    companion object {
        /**
         * Create provider that will create wrap that executes [callBefore] before function body execution and [callAfter]
         * after.
         */
        fun create(callBefore: (FunctionInvocation) -> Unit, callAfter: (Any?) -> Unit): FunctionWrapProvider =
                object : FunctionWrapProvider {

                    override fun provide() = object : FunctionWrap {

                        override fun before(invocation: FunctionInvocation) = callBefore(invocation)

                        override fun after(returnValue: Any?) = callAfter(returnValue)
                    }
                }
    }
}