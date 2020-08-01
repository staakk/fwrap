package fwrap

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

private const val OPTION_ENABLED = "enabled"
internal val KEY_ENABLED = CompilerConfigurationKey<Boolean>("whether the plugin is enabled")

@Suppress("unused") // Used by auto service.
@AutoService(CommandLineProcessor::class)
class FWrapCommandLineProcessor : CommandLineProcessor {
    override val pluginId = "fwrap"

    override val pluginOptions = listOf(
            CliOption(
                    optionName = OPTION_ENABLED,
                    valueDescription = "<true|false>",
                    description = "Whether to enable FWrap plugin"
            )
    )

    override fun processOption(
            option: AbstractCliOption,
            value: String,
            configuration: CompilerConfiguration
    ) = when (option.optionName) {
        OPTION_ENABLED -> configuration.put(KEY_ENABLED, value.toBoolean())
        else -> error("Unexpected config option ${option.optionName}")
    }
}