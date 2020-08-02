package io.github.staakk.fwrap

interface FunctionWrapFactory <T : FunctionWrap> {

    fun create() : T
}