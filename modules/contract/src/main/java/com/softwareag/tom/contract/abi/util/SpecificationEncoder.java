/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.contract.abi.util;

import com.softwareag.tom.contract.abi.ContractInterface;
import com.softwareag.tom.util.HexValueBase;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static com.softwareag.tom.contract.abi.util.ValueEncoder.MAX_BYTE_LENGTH;

/**
 * The <a href="https://github.com/ethereum/wiki/wiki/Ethereum-Contract-ABI">Ethereum Contract ABI</a> encoding for constructors, functions, and events.
 */
public final class SpecificationEncoder {

    private SpecificationEncoder() {}

    public static <T> String encode(ContractInterface.Specification<T> specification, List<T> values) {
        if ("event".equals(specification.getType())) {
            return getEventId(specification);
        } else {
            String specificationId = getFunctionId(specification);
            return encodeParameters(specification, specificationId, values);
        }
    }

    static <T> String encodeParameters(ContractInterface.Specification<T> specification, String specificationId, List<T> values) {
        StringBuilder result = new StringBuilder(specificationId);

        int dynamicDataOffset = specification.getInputParametersSize() * MAX_BYTE_LENGTH;
        StringBuilder dynamicData = new StringBuilder();

        Iterator<? extends ContractInterface.Parameter<T>> parameterIterator = specification.getInputParameters().iterator();
        Iterator<T> valueIterator = values.iterator();

        while (parameterIterator.hasNext() && valueIterator.hasNext()) {
            ContractInterface.Parameter<T> parameter = parameterIterator.next();
            String encodedValue = parameter.encode(valueIterator.next());

            if (parameter.getType().isDynamic()) {
                String encodedDataOffset = ValueEncoder.encodeNumeric(ParameterTypeJava.UINT, BigInteger.valueOf(dynamicDataOffset));
                result.append(encodedDataOffset);
                dynamicData.append(encodedValue);
                dynamicDataOffset += encodedValue.length() >> 1;
            } else {
                result.append(encodedValue);
            }
        }
        result.append(dynamicData);

        return result.toString();
    }

    static <T> String getFunctionSignature(ContractInterface.Specification<T> specification) {
        String params = specification.getInputParameters().stream().map(p -> String.valueOf(p.getType().getName().equals("address") ? "uint160" : p.getType().getName())).collect(Collectors.joining(","));
        return specification.getName() + "(" + params + ")";
    }

    static <T> String getEventSignature(ContractInterface.Specification<T> specification) {
        List<ContractInterface.Parameter<T>> inputParameters = new ArrayList<>(specification.getInputParameters(true));
        inputParameters.addAll(specification.getInputParameters(false));
        String params = inputParameters.stream().map(p -> String.valueOf(p.getType().getName())).collect(Collectors.joining(","));
        return specification.getName() + "(" + params + ")";
    }

    static <T> String getEventId(ContractInterface.Specification<T> specification) {
        String specificationSignature = getEventSignature(specification);
        return HexValueBase.stripPrefix(HexValueBase.getHash(specificationSignature));
    }

    static <T> String getFunctionId(ContractInterface.Specification<T> specification) {
        String specificationSignature = getFunctionSignature(specification);
        return HexValueBase.stripPrefix(HexValueBase.getHash(specificationSignature).substring(0, 10));
    }
}