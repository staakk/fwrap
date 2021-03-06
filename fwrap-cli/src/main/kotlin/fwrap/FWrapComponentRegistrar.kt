package fwrap

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.codegen.extensions.ClassBuilderInterceptorExtension
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration

@Suppress("unused") // Used by auto service.
@AutoService(ComponentRegistrar::class)
class FWrapComponentRegistrar : ComponentRegistrar {
    override fun registerProjectComponents(
            project: MockProject,
            configuration: CompilerConfiguration
    ) {
        if (configuration[KEY_ENABLED] == false) {
            return
        }

        ClassBuilderInterceptorExtension.registerExtension(
                project,
                FWrapClassGenerationInterceptor()
        )
    }
}