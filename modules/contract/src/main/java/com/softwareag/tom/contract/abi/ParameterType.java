/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.contract.abi;

/**
 * ABI types API.
 */
public interface ParameterType<T>  {
    Class<T> getType();
    String getName();
    T asType(Object value);
    /**
     * @return the size of an array for a parameter of type fixed-length array, otherwise returns 1
     */
    default int size() {
        int start = getName().trim().indexOf('[') + 1;
        int end = getName().trim().indexOf(']');
        if (end - start > 0) {
            String length = getName().substring(start,end);
            return Integer.parseInt(length);
        } else {
            return 1;
        }
    }
}
