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
 * @param <T> The expected result type of this contract interface
 */
public abstract class ContractInterface<T extends ContractInterface.Specification> {
    public List<T> entries;

    public abstract List<T> getConstructors();
    public abstract List<T> getFunctions();
    public abstract List<T> getEvents();

    /**
     * Specification API.
     */
    public interface Specification {
        String encode();
    }
}
