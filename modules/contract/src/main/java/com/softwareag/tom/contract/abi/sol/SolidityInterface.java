/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.contract.abi.sol;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.softwareag.tom.contract.abi.ContractInterface;
import com.softwareag.tom.contract.abi.ParameterType;
import com.softwareag.tom.contract.abi.util.SpecificationEncoder;
import com.softwareag.tom.contract.abi.util.ValueEncoder;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Solidity interface implementation.
 */
public class SolidityInterface extends ContractInterface {

    @Override public List<ContractInterface.Specification> getConstructors() {
        return specifications.stream().filter(entry -> ("constructor".equals(entry.getType()))).collect(Collectors.toList());
    }

    @Override public List<ContractInterface.Specification> getFunctions() {
        return specifications.stream().filter(entry -> ("function".equals(entry.getType()))).collect(Collectors.toList());
    }

    @Override public List<ContractInterface.Specification> getEvents() {
        return specifications.stream().filter(entry -> ("event".equals(entry.getType()))).collect(Collectors.toList());
    }

    public static class SoliditySpecification<T> implements ContractInterface.Specification<T> {
        @JsonProperty("name") String name;
        @JsonProperty("type") String type;
        @JsonProperty("inputs") List<SolidityParameter<T>> inputParameters;
        @JsonProperty("outputs") List<SolidityParameter<T>> outputParameters;
        @JsonProperty("constant") boolean constant;
        @JsonProperty("payable") boolean payable;
        @JsonProperty("anonymous") boolean anonymous;

        @Override public String getName() { return name; }
        @Override public String getType() { return type; }
        @Override public List<? extends ContractInterface.Parameter<T>> getInputParameters() { return inputParameters; }
        @Override public List<? extends ContractInterface.Parameter<T>> getOutputParameters() { return outputParameters; }
        @Override public boolean isConstant() { return constant; }
        @Override public boolean isPayable() { return payable; }
        @Override public boolean isAnonymous() { return anonymous; }
    }

    public static class SolidityParameter<T> implements ContractInterface.Parameter<T> {
        @JsonProperty("name") String name;
        @JsonProperty("type") ParameterType<T> type;
        @JsonProperty("indexed") boolean indexed;

        @SuppressWarnings("unused") public void setType(String type) {
            this.type = ValueEncoder.parse(type);
        }

        @Override public String getName() { return name; }
        @Override public ParameterType<T> getType() { return type; }
        @Override public boolean isIndexed() { return indexed; }
    }
}