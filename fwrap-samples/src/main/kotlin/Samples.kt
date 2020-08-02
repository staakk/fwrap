package io.github.staakk.fwrapsamples

import io.github.staakk.fwrap.*

fun main() {
    FunctionWrapProviderRegistry.registerProvider("log", loggingWrapperProvider)
    FunctionWrapProviderRegistry.registerProvider("hello", simpleWrapperProvider)

    functionWithGreeting()
    println()
    functionWithLog(1, "foo")
    println()
    functionWithGreetingAndLog(1, "foo")
}

@Wrap(["hello"])
fun functionWithGreeting() {
    println("...")
}

@Wrap(["log"])
fun functionWithLog(arg0: Int, arg1: String): String {
    return "$arg0 $arg1"
}

@Wrap(["hello", "log"])
fun functionWithGreetingAndLog(arg0: Int, arg1: String): String {
    return "$arg0 $arg1"
}


val simpleWrapperProvider = object : FunctionWrapProvider<FunctionWrap> {

    override fun provide() = object : FunctionWrap {

        private lateinit var invocation: FunctionInvocation

        override fun before(invocation: FunctionInvocation) {
            this.invocation = invocation
            println("Hello from function ${invocation.name}")
        }

        override fun after(returnValue: Any?) {
            println("Good bye from function ${invocation.name}")
        }
    }
}

val loggingWrapperProvider = object : FunctionWrapProvider<FunctionWrap> {

    override fun provide() = object : FunctionWrap {

        override fun before(invocation: FunctionInvocation) {
            println("ENTRY: ${invocation.receiver}.${invocation.name}(${invocation.params.entries.joinToString { (key, value) -> "$key=$value" }})")
        }

        override fun after(returnValue: Any?) {
            println("EXIT: return=$returnValue")
        }
    }
}