/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.contract.abi;

import com.softwareag.tom.contract.abi.util.ValueEncoder;

import java.util.List;

/**
 * Contract interface API.
 */
public abstract class ContractInterface {
    public List<ContractInterface.Specification> specifications;

    public abstract List<ContractInterface.Specification> getConstructors();
    public abstract List<ContractInterface.Specification> getFunctions();
    public abstract List<ContractInterface.Specification> getEvents();

    /**
     * Specification API.
     */
    public interface Specification<T> {
        String getName();
        String getType();
        List<? extends ContractInterface.Parameter<T>> getInputParameters();
        List<? extends ContractInterface.Parameter<T>> getOutputParameters();
        boolean isConstant();
        boolean isPayable();
        boolean isAnonymous();
        /**
         * @return the encoded specification.
         */
        String encode(List<T> values);
    }

    /**
     * Parameter API.
     */
    public interface Parameter<T> {
        String getName();
        ParameterType<T> getType();
        boolean isIndexed();
        /**
         * @return the length of an array for a parameter of type fixed-length array, otherwise returns 1
         */
        short getLength();
        /**
         * @return the encoded parameter value
         */
        default String encode(T value) {
            return ValueEncoder.encode(getType(), value);
        }
    }
}