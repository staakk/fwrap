package io.github.staakk.fwraptest

import io.github.staakk.fwrap.*

fun main() {
    FunctionWrapProviderRegistry.registerProvider("testId", object : FunctionWrapProvider<TestWrapper> {
        override fun provide() = TestWrapper()
    })

    FunctionWrapProviderRegistry.get("testId")
    
    Test2().test(2L, null)
    println()
    Test2().test(2L, null)
}
var d : Int? = 3213213

class Test2 {
    @Wrap(["testId"])
    fun test(l: Long, i: Int?) : Int? {
        println("test")
        return null
    }
}

class TestWrapper: FunctionWrap {

    override fun before(invocation: FunctionInvocation) {
        println("this: $this")
        println("name: ${invocation.name}")
        println("receiver: ${invocation.receiver}")
        invocation.params.forEach { (key, value) ->
            println("$key = $value")
        }
    }

    override fun after(returnValue: Any?) {
        println("this: $this")
        println("after $returnValue")
    }
}
