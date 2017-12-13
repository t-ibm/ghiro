/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.contract.abi.util;

import com.softwareag.tom.contract.abi.ContractInterface;
import com.softwareag.tom.util.Hash;
import com.softwareag.tom.util.HexValueBase;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static com.softwareag.tom.contract.abi.util.ValueEncoder.MAX_BYTE_LENGTH;

/**
 * The <a href="https://github.com/ethereum/wiki/wiki/Ethereum-Contract-ABI">Ethereum Contract ABI</a> encoding for constructors, functions, and events.
 */
public class SpecificationEncoder {

    private SpecificationEncoder() {}

    public static <T, P extends ContractInterface.Parameter<T>> String encode(ContractInterface.Specification<T, P> specification, List<T> values) {
        List<P> parameters = specification.getInputParameters();

        String specificationSignature = getSpecificationSignature(specification.getName(), parameters);
        String specificationId = getSpecificationId(specificationSignature);

        return encodeParameters(parameters, specificationId, values);
    }

    static <T, P extends ContractInterface.Parameter<T>> String encodeParameters(List<P> parameters, String specificationId, List<T> values) {
        StringBuilder result = new StringBuilder(specificationId);

        int dynamicDataOffset = getParameterCount(parameters) * MAX_BYTE_LENGTH;
        StringBuilder dynamicData = new StringBuilder();

        Iterator<P> parameterIterator = parameters.iterator();
        Iterator<T> valueIterator = values.iterator();

        while (parameterIterator.hasNext() && valueIterator.hasNext()) {
            ContractInterface.Parameter<T> parameter = parameterIterator.next();
            String encodedValue = parameter.encode(valueIterator.next());

            if (parameter.getType() == ParameterTypeJava.STRING || parameter.getType() == ParameterTypeJava.BYTES
                || parameter.getType() instanceof ParameterTypeJava.ArrayType && ((ParameterTypeJava.ArrayType)parameter.getType()).isDynamic()) {
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

    static <T, P extends ContractInterface.Parameter<T>> int getParameterCount(List<P> parameters) {
        int count = 0;
        for (P parameter:parameters) {
            count += parameter.getLength();
        }
        return count;
    }

    static <T, P extends ContractInterface.Parameter<T>> String getSpecificationSignature(String methodName, List<P> parameters) {
        String params = parameters.stream().map(p -> String.valueOf(p.getType().getName())).collect(Collectors.joining(","));
        return methodName + "(" + params + ")";
    }

    static String getSpecificationId(String methodSignature) {
        return HexValueBase.stripPrefix(Hash.sha3(HexValueBase.encode(methodSignature)).substring(0, 10));
    }
}