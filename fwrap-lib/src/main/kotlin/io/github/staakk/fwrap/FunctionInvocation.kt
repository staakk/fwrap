package io.github.staakk.fwrap

data class FunctionInvocation(
        val name: String,
        val receiver: Any,
        val params: Map<String, Any>
)