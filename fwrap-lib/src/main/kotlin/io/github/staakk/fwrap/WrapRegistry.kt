package io.github.staakk.fwrap

@Suppress("unused") // Public API.
object WrapRegistry {
    val wraps = mutableMapOf<String, FunctionWrap>()
}