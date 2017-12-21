/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * Object mapper factory.
 */
public class ObjectMapperFactory {

    private static final ObjectMapper jsonMapper = new ObjectMapper();
    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    static {
        jsonMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        yamlMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        yamlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        yamlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    private ObjectMapperFactory() {}

    public static ObjectMapper getJsonMapper() { return jsonMapper; }
    public static ObjectMapper getYamlMapper() { return yamlMapper; }
}
