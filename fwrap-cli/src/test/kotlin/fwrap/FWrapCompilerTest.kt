package fwrap

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode
import com.tschuchort.compiletesting.KotlinCompilation.Result
import com.tschuchort.compiletesting.SourceFile
import org.intellij.lang.annotations.Language
import org.junit.Assert.assertEquals
import org.junit.Test

class FWrapCompilerTest {

    @Test
    fun `top level function`() {
        val result = compile("""
            import io.github.staakk.fwrap.Wrap
            
            @Wrap(["id"])
            fun test() = Unit
        """.trimIndent())

        assertEquals(ExitCode.OK, result.exitCode)
    }

    @Test
    fun `regular function`() {
        val result = compile("""
            import io.github.staakk.fwrap.Wrap

            class Test {
                @Wrap(["id"])
                fun test() = Unit
            }
        """.trimIndent())

        assertEquals(ExitCode.OK, result.exitCode)
    }

    @Test
    fun `companion object function`() {
        val result = compile("""
            import io.github.staakk.fwrap.Wrap
            
            class Test {
                companion object {
                    @Wrap(["id"])
                    fun test() = Unit
                }
            }
        """.trimIndent())

        assertEquals(ExitCode.OK, result.exitCode)
    }

    @Test
    fun `companion object @JvmStatic function`() {
        val result = compile("""
            import io.github.staakk.fwrap.Wrap
            
            class Test {
                companion object {
                    @Wrap(["id"])
                    @JvmStatic
                    fun test() = Unit
                }
            }
        """.trimIndent())

        assertEquals(ExitCode.OK, result.exitCode)
    }

    @Test
    fun `function with argument`() {
        val result = compile("""
            import io.github.staakk.fwrap.Wrap
            
            @Wrap(["id"])
            fun test(i: Int) = Unit
        """.trimIndent())

        assertEquals(ExitCode.OK, result.exitCode)
    }

    @Test
    fun `function with nullable argument`() {
        val result = compile("""
            import io.github.staakk.fwrap.Wrap
            
            @Wrap(["id"])
            fun test(i: Int?) = Unit
        """.trimIndent())

        assertEquals(ExitCode.OK, result.exitCode)
    }

    @Test
    fun `function with return type`() {
        val result = compile("""
            import io.github.staakk.fwrap.Wrap
            
            @Wrap(["id"])
            fun test(i: Int?) = 42
        """.trimIndent())

        assertEquals(ExitCode.OK, result.exitCode)
    }

    @Test
    fun `function with multiple wraps`() {
        val result = compile("""
            import io.github.staakk.fwrap.Wrap
            
            @Wrap(["id1", "id2"])
            fun test(i: Int?) = 42
        """.trimIndent())

        assertEquals(ExitCode.OK, result.exitCode)
    }

    @Test
    fun `function with all argument types`() {
        val result = compile("""
            import io.github.staakk.fwrap.Wrap
            
            @Wrap(["id1", "id2"])
            fun test(b: Boolean, bb: Byte, c: Char, s: Short, i: Int, l: Long, f: Float, d: Double, st: String) = 42
        """.trimIndent())

        assertEquals(ExitCode.OK, result.exitCode)
    }

    private fun compile(@Language("kotlin") source: String): Result {
        return KotlinCompilation().apply {
            sources = listOf(SourceFile.kotlin("test.kt", source))
            messageOutputStream = System.out
            compilerPlugins = listOf(FWrapComponentRegistrar())
            inheritClassPath = true
            useIR = true
        }.compile()
    }

}