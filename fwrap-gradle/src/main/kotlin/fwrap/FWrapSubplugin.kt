package fwrap

import com.google.auto.service.AutoService
import org.gradle.api.Project
import org.gradle.api.tasks.compile.AbstractCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonOptions
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinGradleSubplugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

@AutoService(KotlinGradleSubplugin::class)
class FWrapSubplugin : KotlinGradleSubplugin<AbstractCompile> {
    override fun apply(
            project: Project,
            kotlinCompile: AbstractCompile,
            javaCompile: AbstractCompile?,
            variantData: Any?,
            androidProjectHandler: Any?,
            kotlinCompilation: KotlinCompilation<KotlinCommonOptions>?
    ): List<SubpluginOption> {
        val extension = project.extensions.findByType(FWrapExtension::class.java)
                ?: FWrapExtension()

        return listOf(
                SubpluginOption(key = "enabled", value = extension.enabled.toString())
        )
    }

    override fun getCompilerPluginId() = "fwrap"

    override fun getPluginArtifact() = SubpluginArtifact(
            groupId = "com.github.staakk.fwrap",
            artifactId = "fwrap-cli",
            version = "0.0.2"
    )

    override fun isApplicable(project: Project, task: AbstractCompile) =
            project.plugins.hasPlugin(FWrapPlugin::class.java)

}