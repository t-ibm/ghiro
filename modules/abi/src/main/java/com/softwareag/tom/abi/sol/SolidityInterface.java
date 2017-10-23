/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.abi.sol;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.softwareag.tom.abi.ContractInterface;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Solidity interface implementation.
 */
public class SolidityInterface extends ContractInterface<SolidityInterface.Entries> {

    @Override public List<Entries> getConstructors() {
        return entries.stream().filter(entry -> ("constructor".equals(entry.type))).collect(Collectors.toList());
    }

    @Override public List<Entries> getFunctions() {
        return entries.stream().filter(entry -> ("function".equals(entry.type))).collect(Collectors.toList());
    }

    @Override public List<Entries> getEvents() {
        return entries.stream().filter(entry -> ("event".equals(entry.type))).collect(Collectors.toList());
    }

    public static class Entries implements ContractInterface.Specification {
        @JsonProperty("name") public String name;
        @JsonProperty("type") public String type;
        @JsonProperty("inputs") public List<Parameter> inputs;
        @JsonProperty("outputs") public List<Parameter> outputs;
        @JsonProperty("constant") public boolean constant;
        @JsonProperty("payable") public boolean payable;
        @JsonProperty("anonymous") public boolean anonymous;

        @Override public String encode() {
            return null; //todo
        }
    }

    private static class Parameter {
        @JsonProperty("name") public String name;
        @JsonProperty("type") public String type;
        @JsonProperty("indexed") public boolean indexed;
    }
}