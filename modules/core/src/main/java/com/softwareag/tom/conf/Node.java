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
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;

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
    int size() { return environments.size(); }
    public String getName() { return name; }
    public Host getHost() { return host; }
    public Key getKey() { return key; }
    public Contract getContract() { return contract; }
    public Config getConfig() { return config; }

    public static class Host {
        @JsonProperty("ip") String ip;
        @JsonProperty("grpc") GRpc grpc;
        @JsonProperty("info") Info info;
        @JsonProperty("web3") Web3 web3;
        @JsonProperty("tendermint") Tendermint tendermint;
        public String getIp() { return ip; }
        public GRpc getGRpc() { return grpc; }
        public Info getInfo() { return info; }
        public Web3 getWeb3() { return web3; }
        public Tendermint getTendermint() { return tendermint; }
    }

    public static class GRpc {
        @JsonProperty("port") int port;
        public int getPort() { return port; }
    }

    public static class Info {
        @JsonProperty("port") int port;
        public int getPort() { return port; }
    }

    public static class Web3 {
        @JsonProperty("port") int port;
        public int getPort() { return port; }
    }

    public static class Tendermint {
        @JsonProperty("port") int port;
        public int getPort() { return port; }
    }

    public static class Key {
        @JsonProperty("private") String priv;
        public String getPrivate() { return priv; }
    }

    public static class Config {
        @JsonProperty("location") String location;
        public URI getLocationAsUri() { return convert(location -> Paths.get(location).toUri().normalize()); }
        private <T> T convert(Function<String, T> func) { return func.apply(location); }
    }

    public static class Contract {
        @JsonProperty("registry") Registry registry;
        public Registry getRegistry() { return registry; }

        public static class Registry {
            @JsonProperty("location") String location;
            public URI getLocationAsUri() { return convert(location -> Paths.get(location).toUri().normalize()); }
            private <T> T convert(Function<String, T> func) { return func.apply(location); }
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
     * @throws ExceptionInInitializerError if file {@code Node.yaml} cannot be decoded
     */
    public static Node instance(String name) throws ExceptionInInitializerError {
        try {
            Node defaultConf = getDefaultConf();
            Node namedConf =  defaultConf.environments.stream().filter(o -> o.getName().equals(name)).findFirst().orElse(null);
            ObjectMapperFactory.getYamlMapper().updateValue(defaultConf, namedConf);
            return  defaultConf;
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static Node getDefaultConf() throws IOException {
        InputStream nodeConf = Node.class.getClassLoader().getResourceAsStream("Node.yaml");
        return ObjectMapperFactory.getYamlMapper().readValue(nodeConf, Node.class);
    }
}