/*
 * Copyright 2010 Software AG, Inc.
 * ALL RIGHTS RESERVED
 *
 * UNPUBLISHED -- Rights reserved under the copyright laws of the United States.
 * Use of a copyright notice is precautionary only and does not imply
 * publication or disclosure.
 *
 * THIS SOURCE CODE IS THE CONFIDENTIAL AND PROPRIETARY INFORMATION OF
 * SOFTWARE AG, INC.  ANY REPRODUCTION, MODIFICATION, DISTRIBUTION,
 * OR DISCLOSURE IN ANY FORM, IN WHOLE, OR IN PART, IS STRICTLY PROHIBITED
 * WITHOUT THE PRIOR EXPRESS WRITTEN PERMISSION OF SOFTWARE AG, INC.
 */
package wm.dapp;

// --- <<IS-START-IMPORTS>> ---
import com.softwareag.tom.conf.Node;
import com.softwareag.tom.is.pkg.dapp.DAppLogger;
import com.softwareag.tom.is.pkg.dapp.DAppMsgBundle;
import com.softwareag.tom.is.pkg.dapp.Util;
import com.wm.app.b2b.server.FlowSvcImpl;
import com.wm.app.b2b.server.Package;
import com.wm.app.b2b.server.PackageManager;
import com.wm.app.b2b.server.ServiceException;
import com.wm.app.b2b.server.ns.Namespace;
import com.wm.app.b2b.ws.codegen.FlowGenUtil;
import com.wm.app.b2b.ws.ns.NSFacade;
import com.wm.data.IData;
import com.wm.data.IDataFactory;
import com.wm.lang.flow.FlowInvoke;
import com.wm.lang.ns.NSName;
import com.wm.lang.ns.NSSignature;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
// --- <<IS-END-IMPORTS>> ---

public final class Admin {
    /**
     * Synchronizes the contracts to the IS namespace.
     *
     * @param pipeline The pipeline
     * @throws ServiceException If there is an error during execution of this service
     */
    public static void syncContracts(IData pipeline) throws ServiceException {
        // --- <<IS-START(syncContracts)>> ---
        // @subtype unknown
        // @sigtype java 3.5
        Package pkg = PackageManager.getPackage("WmDApp");
        System.setProperty(Node.SYSTEM_PROPERTY_TOMCONFNODE, String.valueOf(pkg.getManifest().getProperty("node")));
        Map<NSName,NSSignature> nsNodes;
        Set<NSName> contractNodes = new HashSet<>();
        try {
            nsNodes = Util.create().getFunctions();
        } catch (IOException e) {
            throw new ServiceException(e);
        }
        try {
            for (Map.Entry<NSName,NSSignature> nsNode : nsNodes.entrySet()) {
                NSName nsName = nsNode.getKey();
                if (!contractNodes.contains(nsName.getInterfaceNSName())) {
                    // Remember the contract nodes
                    contractNodes.add(nsName.getInterfaceNSName());
                }
                NSSignature nsSignature = nsNode.getValue();
                if (!pkg.getStore().getNodePath(nsName).mkdirs()) {
                    DAppLogger.logDebug(DAppMsgBundle.DAPP_SERVICES_MKDIRS, new Object[]{""+nsName});
                }
                FlowSvcImpl flowSvcImpl = FlowGenUtil.getFlowSvcImpl(pkg, nsName, nsSignature, "dapp");
                FlowInvoke flowInvoke = FlowGenUtil.getFlowInvoke("wm.dapp.Contract:call");
                flowSvcImpl.getFlowRoot().addNode(flowInvoke);
                NSFacade.saveNewNSNode(flowSvcImpl);
            }
            for (NSName contractNode : contractNodes) {
                // Add the load service
                NSName nsName = NSName.create(contractNode.toString(), "load");
                NSSignature nsSignature = NSSignature.create(Namespace.current(), IDataFactory.create());
                FlowSvcImpl flowSvcImpl = FlowGenUtil.getFlowSvcImpl(pkg, nsName, nsSignature, "dapp");
                FlowInvoke flowInvoke = FlowGenUtil.getFlowInvoke("wm.dapp.Contract:load");
                flowSvcImpl.getFlowRoot().addNode(flowInvoke);
                NSFacade.saveNewNSNode(flowSvcImpl);
                // Add the deploy service
                nsName = NSName.create(contractNode.toString(), "deploy");
                flowSvcImpl = FlowGenUtil.getFlowSvcImpl(pkg, nsName, nsSignature, "dapp");
                flowInvoke = FlowGenUtil.getFlowInvoke("wm.dapp.Contract:deploy");
                flowSvcImpl.getFlowRoot().addNode(flowInvoke);
                NSFacade.saveNewNSNode(flowSvcImpl);
            }
        } catch (Exception e) {
            throw new ServiceException(e);
        }
        // --- <<IS-END>> ---
    }
}