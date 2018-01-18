/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.contract.abi;

import com.softwareag.tom.contract.abi.util.ReturnDecoder;
import com.softwareag.tom.contract.abi.util.SpecificationEncoder;
import com.softwareag.tom.contract.abi.util.ValueDecoder;
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
         * @param values A list of Java values
         * @return the encoded specification as a hex string
         */
        default String encode(List<T> values) {
            return SpecificationEncoder.encode(this, values);
        }
        /**
         * @param value The returned hex string
         * @return a list of Java values
         */
        default List<T> decode(String value) {
            return ReturnDecoder.decode(getOutputParameters(), value);
        }
        /**
         * @return the input parameter size. Note, that this value will only be equal to the input parameter count if all input parameters are simple types
         */
        default int getInputParametersSize() {
            int count = 0;
            for (ContractInterface.Parameter<T> parameter : getInputParameters()) {
                count += parameter.getType().size();
            }
            return count;
        }
    }

    /**
     * Parameter API.
     */
    public interface Parameter<T> {
        String getName();
        ParameterType<T> getType();
        boolean isIndexed();
        /**
         * @param value The value as a Java type
         * @return the value as hex string
         */
        default String encode(T value) {
            return ValueEncoder.encode(getType(), value);
        }
        /**
         * @param value The value as hex string
         * @param offset The index of the hex string where this particular value starts
         * @return the value as a Java type
         */
        default T decode(String value, int offset) {
            return ValueDecoder.decode(getType(), value, offset);
        }
    }
}