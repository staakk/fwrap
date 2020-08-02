package io.github.staakk.fwrap

/**
 * Annotated function will be wrapped by [FunctionWrap] registered with given [id].
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Suppress("unused") // Public API.
annotation class Wrap(
    val id: String
)