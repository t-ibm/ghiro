/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.abi.sol;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.softwareag.tom.abi.ContractInterface;
import com.softwareag.tom.abi.ParameterType;
import com.softwareag.tom.abi.util.SpecificationEncoder;
import com.softwareag.tom.abi.util.ValueEncoder;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Solidity interface implementation.
 */
public class SolidityInterface extends ContractInterface<SolidityInterface.SoliditySpecification> {

    @Override public List<SoliditySpecification> getConstructors() {
        return specifications.stream().filter(entry -> ("constructor".equals(entry.type))).collect(Collectors.toList());
    }

    @Override public List<SoliditySpecification> getFunctions() {
        return specifications.stream().filter(entry -> ("function".equals(entry.type))).collect(Collectors.toList());
    }

    @Override public List<SoliditySpecification> getEvents() {
        return specifications.stream().filter(entry -> ("event".equals(entry.type))).collect(Collectors.toList());
    }

    public static class SoliditySpecification<T> implements ContractInterface.Specification<T, SolidityParameter<T>> {
        @JsonProperty("name") String name;
        @JsonProperty("type") String type;
        @JsonProperty("inputs") List<SolidityParameter<T>> inputParameters;
        @JsonProperty("outputs") List<SolidityParameter<T>> outputParameters;
        @JsonProperty("constant") boolean constant;
        @JsonProperty("payable") boolean payable;
        @JsonProperty("anonymous") boolean anonymous;

        @Override public String getName() { return name; }
        @Override public String getType() { return type; }
        @Override public List<SolidityParameter<T>> getInputParameters() {
            return inputParameters;
        }
        @Override public List<SolidityParameter<T>> getOutputParameters() { return outputParameters; }
        @Override public boolean getConstant() { return constant; }
        @Override public boolean getPayable() { return payable; }
        @Override public boolean getAnonymous() { return anonymous; }

        @Override public String encode(List<T> values) {
            return SpecificationEncoder.encode(this, values);
        }
    }

    public static class SolidityParameter<T> implements ContractInterface.Parameter<T> {
        @JsonProperty("name") String name;
        @JsonProperty("type") ParameterType type;
        @JsonProperty("indexed") boolean indexed;

        @SuppressWarnings("unused") public void setType(String type) {
            this.type = ValueEncoder.parse(type);
        }

        @Override public String getName() { return name; }
        @Override public ParameterType getType() { return type; }
        @Override public boolean getIndexed() { return indexed; }

        @Override public short getLength() {
            int start = type.getName().trim().indexOf('[') + 1;
            int end = type.getName().trim().indexOf(']');
            if (end - start > 0) {
                String length = type.getName().substring(start,end);
                return Short.parseShort(length);
            } else {
                return 1;
            }
        }
    }
}