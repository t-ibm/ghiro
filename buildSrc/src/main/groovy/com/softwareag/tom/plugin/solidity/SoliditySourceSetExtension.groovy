/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2018 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or
 * its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.plugin.solidity

import org.ethereum.solidity.compiler.SolidityCompiler
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.util.PatternSet

import static org.ethereum.solidity.compiler.SolidityCompiler.Options.*

/**
 * An extension defining a set of solidity sources and compiler options.
*/
class SoliditySourceSetExtension implements Serializable {
    private transient final Project project
    private List<String> directories = new ArrayList<>()
    private List<SolidityCompiler.Options> enumOptions = new ArrayList<>()
    /**
     * Declaring a new source directory set for {@code .sol} files matching the standard layout. Can always be overridden.
     * @param project The project
     * @param sourceSetName The source set name
     */
    SoliditySourceSetExtension(Project project, String sourceSetName) {
        this.project = project
        srcDir("src/$sourceSetName/solidity")
        enumOptions(*[ABI, BIN, INTERFACE, METADATA])
    }
    /**
     * @return a map of {@code .sol} files found, keyed by the containing source folder
     */
    Map<String, FileCollection> getSolidity() {
        PatternSet patternSet = new PatternSet().include("**/*.sol")
        def files = new HashMap()
        for (String folder : directories) {
            files.put(folder, project.fileTree(folder).matching(patternSet))
        }
        return files
    }
    void srcDir(String dir) {
        directories.add(dir)
    }
    void srcDirs(String... dirs) {
        Collections.addAll(directories, dirs)
    }
    void setSrcDirs(Iterable<String> dirs) {
        directories = new ArrayList<>()
        directories.addAll(dirs)
    }
    /**
     * @return the enum options
     */
    SolidityCompiler.Options[] getEnumOptions() {
        return enumOptions
    }
    void enumOption(SolidityCompiler.Options option) {
        enumOptions.add(option)
    }
    void enumOptions(SolidityCompiler.Options... options) {
        Collections.addAll(enumOptions, options)
    }
    void setEnumOptions(Iterable<SolidityCompiler.Options> options) {
        enumOptions = new ArrayList<>()
        enumOptions.addAll(options)
    }
}