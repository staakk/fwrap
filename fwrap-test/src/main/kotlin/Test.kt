package io.github.staakk.fwraptest

import io.github.staakk.fwrap.FunctionWrap
import io.github.staakk.fwrap.Wrap
import io.github.staakk.fwrap.WrapRegistry

fun main() {
    WrapRegistry.wraps["testId"] = TestWrapper()
    test()
}

@Wrap("testId")
fun test() {
    println("test")
}

class TestWrapper: FunctionWrap {

    override fun before() {
        println("before")
    }

    override fun after() {
        println("after")
    }
}
