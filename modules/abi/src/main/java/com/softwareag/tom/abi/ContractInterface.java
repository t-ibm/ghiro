/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.abi;

import java.util.List;

/**
 * Contract interface API.
 * @param <S> The expected specification type of this contract interface
 */
public abstract class ContractInterface<S extends ContractInterface.Specification> {
    public List<S> specifications;

    public abstract List<S> getConstructors();
    public abstract List<S> getFunctions();
    public abstract List<S> getEvents();

    /**
     * Specification API.
     * @param <P> The expected parameter type of this contract interface
     */
    public interface Specification<P extends ContractInterface.Parameter> {
        String getName();
        String getType();
        List<P> getInputParameters();
        List<P> getOutputParameters();
        boolean getConstant();
        boolean getPayable();
        boolean getAnonymous();

        String encode();
    }

    /**
     * Parameter API.
     */
    public interface Parameter {
        String getName();
        String getType();
        boolean getIndexed();
        /**
         * @return the length of an array for a parameter of type fixed-length array, otherwise returns 1.
         */
        short getLength();
    }
}
