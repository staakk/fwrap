package io.github.staakk.fwraptest

import io.github.staakk.fwrap.FunctionInvocation
import io.github.staakk.fwrap.FunctionWrap
import io.github.staakk.fwrap.Wrap
import io.github.staakk.fwrap.WrapRegistry

fun main() {
    WrapRegistry.wraps["testId"] = TestWrapper()
    Test2().test(1)
}

class Test2 {
    @Wrap("testId")
    fun test(i: Int): Int {
        println("test")
        return 1876
    }
}

class TestWrapper: FunctionWrap {

    override fun before(invocation: FunctionInvocation) {
        println("name: ${invocation.name}")
        println("receiver: ${invocation.receiver}")
        invocation.params.forEach { (key, value) ->
            println("$key = $value")
        }
    }

    override fun after() {
        println("after")
    }
}
