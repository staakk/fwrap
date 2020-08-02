package io.github.staakk.fwrap

/**
 * Represents invoked function.
 */
data class FunctionInvocation(

        /**
         * Name of the function.
         */
        val name: String,

        /**
         * Receiver of the function.
         */
        val receiver: Any,

        /**
         * Parameters of the function. Keys contain parameter name and values are parameter's values.
         */
        val params: Map<String, Any>
)