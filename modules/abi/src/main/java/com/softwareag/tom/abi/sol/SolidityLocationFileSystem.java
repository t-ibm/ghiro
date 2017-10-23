/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.abi.sol;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softwareag.tom.ObjectMapperFactory;
import com.softwareag.tom.abi.Contract;
import com.softwareag.tom.abi.ContractInterface;
import com.softwareag.tom.abi.ContractLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Solidity fs location implementation.
 */
public class SolidityLocationFileSystem implements ContractLocation {

    private static final Logger logger = LoggerFactory.getLogger(SolidityLocationFileSystem.class);
    private final ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
    private final File rootDirectory;

    public SolidityLocationFileSystem(String rootDirectoryName) throws IOException {
        rootDirectory = new File(rootDirectoryName);
        if (!rootDirectory.exists()) {
            throw new IOException("File system location does not exist: " + rootDirectory.getCanonicalPath());
        } else  if (!rootDirectory.isDirectory()) {
            throw new IOException("File system location is not a directory: " + rootDirectory.getCanonicalPath());
        }
    }

    @Override public Map<String, Contract> load() throws IOException {
        Map<String, Contract> contracts = new HashMap<>();
        Files.find(Paths.get(rootDirectory.toURI()), 64, (path, bfa) -> bfa.isRegularFile() && path.getFileName().toString().matches(".*\\.bin")).forEachOrdered(
            pathBin -> {
                URI id = rootDirectory.toURI().relativize(pathBin.toUri());
                id = URI.create(id.toString().substring(0, id.toString().lastIndexOf('.'))); //Remove extension from URI
                logger.info("Loading contract: " + id);
                Path pathAbi = FileSystems.getDefault().getPath(rootDirectory.toString(), id.toString() + ".abi");
                try {
                    Contract contract = Contract.create(getContractInterface(Files.readAllBytes(pathAbi)), new String(Files.readAllBytes(pathBin)));
                    contracts.put(id.toString(), contract);
                } catch (IOException e) {
                    logger.warn("Unable to read file content, got exception: " + e);
                }
            }
        );
        return contracts;
    }

    private ContractInterface getContractInterface(byte[] src) throws IOException {
        ContractInterface<SolidityInterface.Entries> contractInterface = new SolidityInterface();
        contractInterface.entries = Arrays.asList(objectMapper.readValue(src, SolidityInterface.Entries[].class));
        return contractInterface;
    }
}
