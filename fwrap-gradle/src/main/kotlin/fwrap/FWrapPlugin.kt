package fwrap

import org.gradle.api.Plugin
import org.gradle.api.Project


class FWrapPlugin: Plugin<Project> {
    override fun apply(project: Project) {
        project
                .extensions
                .create("fwrap", FWrapExtension::class.java)
    }
}