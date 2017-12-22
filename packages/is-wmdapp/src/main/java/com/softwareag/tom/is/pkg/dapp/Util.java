/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.is.pkg.dapp;

import com.softwareag.tom.conf.Node;
import com.softwareag.tom.contract.Contract;
import com.softwareag.tom.contract.ContractRegistry;
import com.softwareag.tom.contract.SolidityLocationFileSystem;
import com.softwareag.tom.contract.abi.ContractInterface;
import com.softwareag.tom.contract.abi.ParameterType;
import com.wm.app.b2b.server.ns.Namespace;
import com.wm.data.IDataFactory;
import com.wm.lang.ns.NSField;
import com.wm.lang.ns.NSName;
import com.wm.lang.ns.NSRecord;
import com.wm.lang.ns.NSSignature;
import com.wm.util.JavaWrapperType;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Util {
    private Map<NSName,NSSignature> nsNodes;
    private Util() { nsNodes = new HashMap<>(); }
    public static Util create() { return new Util(); }

    /**
     * @return the contracts as a {@link NSName}/{@link NSSignature} map
     * @throws IOException if the contracts cannot be loaded from the registry
     */
    public Map<NSName,NSSignature> getFunctions() throws IOException {
        Map<String, Contract> contracts;
        File contractRegistryLocation = new File(Node.instance().getContract().getRegistry().getLocation().getPath());
        ContractRegistry contractRegistry = ContractRegistry.build(new SolidityLocationFileSystem(contractRegistryLocation));
        contracts = contractRegistry.load();
        for (Map.Entry<String, Contract> entry : contracts.entrySet()) {
            String folderName = entry.getKey();
            ContractInterface contractInterface = entry.getValue().getContractAbi();
            List<ContractInterface.Specification> functions = contractInterface.getFunctions();
            for (ContractInterface.Specification<?> function : functions) {
                String functionName = function.getName();
                NSName nsName = NSName.create(folderName.replaceAll("/", "."), functionName);
                NSSignature nsSignature = getSignature(nsName, function);
                nsNodes.put(nsName, nsSignature);
            }
        }
        return nsNodes;
    }

    private <T> NSSignature getSignature(NSName nsName, ContractInterface.Specification<T> function) {
        // If the same ns node with a different signature already exists we simply add to the existing signature ...
        NSSignature nsSignature = nsNodes.getOrDefault(nsName, NSSignature.create(Namespace.current(), IDataFactory.create()));
        // ... but make the parameters optional
        boolean optional = nsNodes.containsKey(nsName);

        // Input
        NSRecord inputRecord = getNsRecord(function.getInputParameters(), optional);
        nsSignature.setInput(inputRecord);

        // Output
        NSRecord outputRecord = getNsRecord(function.getOutputParameters(), optional);
        nsSignature.setOutput(outputRecord);

        return nsSignature;
    }

    private <T> NSRecord getNsRecord(List<? extends ContractInterface.Parameter<T>> parameters, boolean optional) {
        NSRecord nsRecord = new NSRecord(Namespace.current());
        for (ContractInterface.Parameter parameter : parameters) {
            String parameterName = parameter.getName();
            ParameterType parameterType = parameter.getType();
            NSField nsField;
            if (parameterType.getType() == String.class) {
                nsRecord.addField(parameterName, NSField.FIELD_STRING, NSField.DIM_SCALAR);
            } else if (parameterType.getType() == List.class) {
                nsRecord.addField(parameterName, NSField.FIELD_OBJECT, NSField.DIM_ARRAY);
            } else {
                nsField = nsRecord.addField(parameterName, NSField.FIELD_OBJECT, NSField.DIM_SCALAR);
                nsField.setJavaWrapperType(getJavaWrapperType(parameterType));
            }
        }
        // Mark existing and added fields as optional
        for (NSField nsField : nsRecord.getFields()) {
            nsField.setOptional(optional);
        }
        return nsRecord;
    }

    private int getJavaWrapperType(ParameterType parameterType) {
        Class javaClass = parameterType.getType();
        int javaWrapperType = JavaWrapperType.JAVA_TYPE_UNKNOWN;
        if (javaClass.equals(Boolean.class)) {
            javaWrapperType = JavaWrapperType.JAVA_TYPE_BOOLEAN;
        } else if (javaClass.equals(BigInteger.class)) {
            javaWrapperType = JavaWrapperType.JAVA_TYPE_BIG_INTEGER;
        } else if (javaClass.equals(byte[].class)) {
            javaWrapperType = JavaWrapperType.JAVA_TYPE_byte_ARRAY;
        }
        return  javaWrapperType;
    }
}