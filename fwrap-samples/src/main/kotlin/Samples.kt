package io.github.staakk.fwrapsamples

import io.github.staakk.fwrap.*
import io.github.staakk.fwrap.FunctionWrapProviderRegistry.registerProvider

fun main() {
    registerProvider(
        id = "log",
        callBefore = {
            println("ENTRY: ${it.receiver}.${it.name}(${it.params.entries.joinToString { (key, value) -> "$key=$value" }})")
        },
        callAfter = {
            println("EXIT: return=$it")
        }
    )
    registerProvider("hello") {
        object : FunctionWrap {
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

fun a(b: Boolean) {

}