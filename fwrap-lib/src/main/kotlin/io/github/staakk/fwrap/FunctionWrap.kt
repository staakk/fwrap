package io.github.staakk.fwrap

/**
 * Interface for wrapping function invocation.
 */
@Suppress("unused") // Public API.
interface FunctionWrap {

    /**
     * This method is invoked before code of the function is executed.
     *
     * @param invocation Contains information about function invocation.
     */
    fun before(invocation: FunctionInvocation)

    /**
     * This method is invoked just before return statement from wrapped function.
     *
     * @param returnValue Value to be returned from the function.
     */
    fun after(returnValue: Any?)
}