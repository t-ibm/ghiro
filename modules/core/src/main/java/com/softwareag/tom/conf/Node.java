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
import java.util.List;

/**
 * Node configuration.
 */
public class Node {
    public static final String SYSTEM_PROPERTY_TOMCONFNODE = "com.softwareag.tom.conf.node";
    @JsonProperty("environments")  private List<Node> environments;
    @JsonProperty("name") private String name;
    @JsonProperty("host") private Host host;
    @JsonProperty("contract")  private Contract contract;
    public List<Node> getEnvironments() { return environments; }
    public String getName() { return name; }
    public Host getHost() { return host; }
    public Contract getContract() { return contract; }

    public static class Host {
        @JsonProperty("ip") String ip;
        public String getIp() { return ip; }
    }

    public static class Contract {
        @JsonProperty("registry") Registry registry;
        public Registry getRegistry() { return registry; }

        public static class Registry {
            @JsonProperty("location") String location;
            public URI getLocation() { return URI.create(location).normalize(); }
        }
    }

    /**
     * @return the default configuration instance as specified in file {@code Node.yaml}
     * @throws IOException if file {@code Node.yaml} cannot be decoded
     */
    public static Node instance() throws IOException {
        String environment = System.getProperty(SYSTEM_PROPERTY_TOMCONFNODE);
        if (environment != null && !environment.isEmpty()) {
            return instance(System.getProperty(SYSTEM_PROPERTY_TOMCONFNODE));
        }
        return getDefaultConf();
    }

    /**
     * @param name The node name
     * @return a named configuration instance as specified in the {@code environments} section of file {@code Node.yaml}
     * @throws IOException if file {@code Node.yaml} cannot be decoded
     */
    static Node instance(String name) throws IOException {
        Node defaultConf = getDefaultConf();
        Node namedConf =  defaultConf.getEnvironments().stream().filter(o -> o.getName().equals(name)).findFirst().orElse(null);
        ObjectMapperFactory.getYamlMapper().updateValue(defaultConf, namedConf);
        return  defaultConf;
    }

    private static Node getDefaultConf() throws IOException {
        InputStream nodeConf = Node.class.getClassLoader().getResourceAsStream("Node.yaml");
        return ObjectMapperFactory.getYamlMapper().readValue(nodeConf, Node.class);
    }
}