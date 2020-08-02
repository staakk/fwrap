package io.github.staakk.fwrap

/**
 * Annotated function will be wrapped by [FunctionWrap]s registered with given [id]s.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Suppress("unused") // Public API.
annotation class Wrap(
    val ids: Array<String>
)