/*
 * Copyright (c) 2018 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.contract;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.softwareag.tom.ObjectMapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Solidity fs location implementation.
 */
public class ConfigLocationFileSystem implements ConfigLocation {

    private static final Logger logger = LoggerFactory.getLogger(ConfigLocationFileSystem.class);
    private final ObjectMapper objectMapper = ObjectMapperFactory.getJsonMapper();
    private final URI configUri;

    public ConfigLocationFileSystem(URI configUri) throws IOException {
        this.configUri = configUri;
        Files.createDirectories(Paths.get(configUri));
        if (!Files.exists(Paths.get(configUri))) {
            logger.debug("File system location '{}' already exists or creation failed.", configUri);
        } else  if (!Files.isDirectory(Paths.get(configUri))) {
            throw new NotDirectoryException(configUri.getPath());
        }
    }

    @Override public Map<String, String> loadContractAddresses() throws IOException {
        TypeReference<HashMap<String,String>> typeRef = new TypeReference<HashMap<String,String>>(){};
        logger.info("Loading contract-address mapping from '{}'.", configUri);
        Path path = Paths.get(configUri.resolve("contract-addresses.json"));
        if (Files.exists(path)) {
            return objectMapper.readValue(Files.readAllBytes(path), typeRef);
        } else {
            return new HashMap<>();
        }
    }

    @Override public Map<String, Contract> storeContractAddresses(Map<String, Contract> contracts) throws IOException {
        Map<String, String> contractAddresses = new HashMap<>();
        for (Map.Entry<String, Contract> entry : contracts.entrySet()) {
            if (entry.getValue().getContractAddress() != null) {
                contractAddresses.put(entry.getKey(), entry.getValue().getContractAddress());
            }
        }
        TypeReference<HashMap<String,String>> typeRef = new TypeReference<HashMap<String,String>>(){};
        logger.info("Storing contract-address mapping to '{}'.", configUri);
        Path path = Paths.get(configUri.resolve("contract-addresses.json"));
        objectMapper.writerFor(typeRef).writeValue(path.toFile(), contractAddresses);
        return  contracts;
    }
}
