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
    @JsonProperty("key") private Key key;
    @JsonProperty("config")  private Config config;
    @JsonProperty("contract")  private Contract contract;
    public List<Node> getEnvironments() { return environments; }
    public String getName() { return validate(name); }
    public Host getHost() { return validate(host); }
    public Key getKey() { return validate(key); }
    public Contract getContract() { return validate(contract); }
    public Config getConfig() { return validate(config); }

    public static class Host {
        @JsonProperty("ip") String ip;
        public String getIp() { return validate(ip); }
    }

    public static class Key {
        @JsonProperty("private") String priv;
        public String getPrivate() { return validate(priv); }
    }

    public static class Config {
        @JsonProperty("location") String location;
        public URI getLocation() { return URI.create(validate(location)).normalize(); }
    }

    public static class Contract {
        @JsonProperty("registry") Registry registry;
        public Registry getRegistry() { return validate(registry); }

        public static class Registry {
            @JsonProperty("location") String location;
            public URI getLocation() { return URI.create(validate(location)).normalize(); }
        }
    }

    static <T> T validate(T value) {
        if (value == null) {
            throw new IllegalArgumentException("Property missing from node configuration.");
        }
        return value;
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