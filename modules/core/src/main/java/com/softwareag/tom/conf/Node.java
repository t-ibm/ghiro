/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.conf;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.softwareag.tom.ObjectMapperFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public class Node {
    @JsonProperty("name") private String name;
    @JsonProperty("contract")  private Contract contract;

    public Contract getContract() { return contract; }
    public String getName() { return name; }

    public static class Contract {
        @JsonProperty("registry") Registry registry;

        public Registry getRegistry() { return registry; }

        public static class Registry {
            @JsonProperty("location") String location;

            public URI getLocation() { return URI.create(location).normalize(); }
        }
    }

    public static Node instance() throws IOException {
        InputStream nodeConf = Node.class.getClassLoader().getResourceAsStream("Node.yaml");
        return ObjectMapperFactory.getYamlMapper().readValue(nodeConf, Node.class);
    }
}
