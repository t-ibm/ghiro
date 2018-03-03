/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2018 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or
 * its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.plugin.solidity

import org.ethereum.solidity.compiler.CompilationResult
import org.ethereum.solidity.compiler.SolidityCompiler
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.util.PatternSet

/**
 * Task to compile a set of .sol files with the Solidity compiler.
 */
class SolidityCompileTask extends DefaultTask {
    @Input Collection<SoliditySourceSetExtension> configurations
    @OutputDirectory File outputDir

    @TaskAction void compile() {
        PatternSet patternSet = new PatternSet().include("**/*.sol")
        for (SoliditySourceSetExtension configuration : getConfigurations()) {
            SolidityCompiler.Option[] enumOptions = configuration.getEnumOptions()

            for (Map.Entry<String, FileCollection> entry : configuration.getSolidity().entrySet()) {
                File sourceDir = project.file(entry.key)
                FileCollection sourceFiles = project.fileTree(sourceDir).matching(patternSet)

                sourceFiles.each { file ->
                    SolidityCompiler.Result res = SolidityCompiler.compile(file, true, enumOptions)
                    if (res.errors != null && res.errors.length() > 0) {
                        throw new GradleException(res.errors)
                    } else {
                        project.logger.info("$path::result == $res.output")
                    }
                    CompilationResult result = CompilationResult.parse(res.output)
                    assert result.contractKeys.size() == result.contracts.size()
                    [result.contractKeys,result.contracts].transpose().each { String contractKey, CompilationResult.ContractMetadata contractMetadata ->
                        File solFile = new File(contractKey.substring(0, contractKey.lastIndexOf(':')))
                        if (solFile.exists() && solFile.canonicalFile == file.canonicalFile) {
                            File nsDir = new File("$outputDir/${sourceDir.toPath().relativize(solFile.toPath()).toFile().parentFile}")
                            nsDir.mkdirs()
                            File abiFile = new File(nsDir, solFile.name.replace('.sol', '.abi'))
                            abiFile.write contractMetadata.abi
                            File binFile = new File(nsDir, solFile.name.replace('.sol', '.bin'))
                            binFile.write contractMetadata.bin
                            if (contractMetadata.solInterface != null) {
                                File ifcFile = new File(nsDir, solFile.name.replace('.sol', '.ifc'))
                                ifcFile.write contractMetadata.solInterface
                            }
                            File mdFile = new File(nsDir, solFile.name.replace('.sol', '.md'))
                            mdFile.write contractMetadata.metadata
                        }
                    }
                }
            }
        }
    }
}