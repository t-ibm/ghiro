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
import com.wm.app.b2b.server.ns.Namespace;
import com.wm.data.IDataFactory;
import com.wm.lang.ns.NSName;
import com.wm.lang.ns.NSSignature;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Util {
    private Map<NSName,NSSignature> nsNodes;
    private Util() { nsNodes = new HashMap<>(); }
    public static Util create() { return new Util(); }

    /**
     * Retrieves a list of contracts as {@link NSName} objects.
     *
     * @throws IOException If the contracts cannot be loaded from the registry
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
            for (ContractInterface.Specification function : functions) {
                String functionName = function.getName();
                NSName nsName = NSName.create(folderName.replaceAll("/", "."), functionName);
                NSSignature nsSignature = getSignature(nsName, function.getInputParameters());
                nsNodes.put(nsName, nsSignature);
            }
        }
        return nsNodes;
    }

    private NSSignature getSignature(NSName nsName, List<ContractInterface.Parameter> inputParameters) {
        return NSSignature.create(Namespace.current(), IDataFactory.create());
    }
}