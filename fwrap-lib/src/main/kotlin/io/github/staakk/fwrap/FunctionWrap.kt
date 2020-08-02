package io.github.staakk.fwrap

@Suppress("unused") // Public API.
interface FunctionWrap {

    fun before(invocation: FunctionInvocation)

    fun after(returnValue: Any?)
}