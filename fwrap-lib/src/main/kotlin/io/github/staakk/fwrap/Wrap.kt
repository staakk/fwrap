package io.github.staakk.fwrap

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Suppress("unused") // Public API.
annotation class Wrap(
    val id: String
)