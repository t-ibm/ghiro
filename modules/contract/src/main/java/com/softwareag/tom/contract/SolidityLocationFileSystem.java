/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.contract;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softwareag.tom.ObjectMapperFactory;
import com.softwareag.tom.contract.abi.ContractInterface;
import com.softwareag.tom.contract.abi.sol.SolidityInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
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
    private final ObjectMapper objectMapper = ObjectMapperFactory.getJsonMapper();
    private final URI rootUri;

    public SolidityLocationFileSystem(URI rootUri) throws IOException {
        this.rootUri = rootUri;
        if (!Files.exists(Paths.get(rootUri))) {
            throw new NoSuchFileException(rootUri.getPath());
        } else  if (!Files.isDirectory(Paths.get(rootUri))) {
            throw new NotDirectoryException(rootUri.getPath());
        }
    }

    @Override public Map<String, Contract> load() throws IOException {
        Map<String, Contract> contracts = new HashMap<>();
        Files.find(Paths.get(rootUri), 64, (path, bfa) -> bfa.isRegularFile() && path.getFileName().toString().matches(".*\\.bin")).forEachOrdered(
            pathBin -> {
                URI uri = rootUri.relativize(pathBin.toUri());
                uri = URI.create(uri.toString().substring(0, uri.toString().lastIndexOf('.'))); //Remove extension from URI
                logger.info("Loading contract '{}'.", uri);
                Path pathAbi = Paths.get(rootUri.resolve(uri.toString() + ".abi"));
                try {
                    Contract contract = Contract.create(getContractInterface(Files.readAllBytes(pathAbi)), new String(Files.readAllBytes(pathBin)));
                    contracts.put(uri.toString(), contract);
                } catch (IOException e) {
                    logger.warn("Unable to read file content, got exception: " + e);
                }
            }
        );
        return contracts;
    }

    private ContractInterface getContractInterface(byte[] src) throws IOException {
        ContractInterface contractInterface = new SolidityInterface();
        contractInterface.specifications = Arrays.asList(objectMapper.readValue(src, SolidityInterface.SoliditySpecification[].class));
        return contractInterface;
    }
}
