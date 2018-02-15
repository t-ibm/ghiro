/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2018 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or
 * its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.plugin.solidity

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.language.jvm.tasks.ProcessResources

/**
 * A plugin for adding Solidity support to {@link JavaPlugin java projects}.
 */
class SolidityPlugin implements Plugin<Project> {
    @Override void apply(Project project) {
        if (project.plugins.hasPlugin(JavaPlugin.class)) {
            applyJava(project)
        } else {
            throw new GradleException("Solidity plugin requires the Java plugin to be applied.")
        }
    }

    private static void applyJava(Project project) {
        project.sourceSets.all { sourceSet ->
            // Create a 'solidity' extension on this source set
            addExtensionToSourceSet(project, sourceSet)

            // Add a Solidity compile task to each source set
            String sourceSetName = (String) sourceSet.name
            String taskName = "main" == sourceSetName ? "" : sourceSetName
            SolidityCompileTask task = createSolidityCompileTask(project, taskName, sourceSetName, [sourceSet])
            task.description = "Compiles $sourceSetName Solidity source."
            ProcessResources processResources = (ProcessResources) project.tasks.getByName("process${taskName.capitalize()}Resources")
            processResources.dependsOn(task)
        }
    }

    private static void addExtensionToSourceSet(Project project, def sourceSet) {
        sourceSet.extensions.create('solidity', SoliditySourceSetExtension, project, sourceSet.name)
    }

    private static SolidityCompileTask createSolidityCompileTask(Project project, String name, String dirName, Collection<?> sourceSets) {
        List<SoliditySourceSetExtension> extensions = new ArrayList<>(sourceSets.size())
        sourceSets.each { sourceSet ->
            extensions.add((SoliditySourceSetExtension) sourceSet.extensions['solidity'])
        }

        SolidityCompileTask task = project.tasks.create("compile${name.capitalize()}Solidity", SolidityCompileTask)
        task.configurations = extensions
        task.outputDir = project.file("${project.buildDir}/solidity/${dirName}")
        return task
    }
}