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
import com.softwareag.tom.is.pkg.dapp.Event;
import com.softwareag.tom.is.pkg.dapp.Util;
import com.wm.app.b2b.server.FlowSvcImpl;
import com.wm.app.b2b.server.ServiceException;
import com.wm.app.b2b.server.dispatcher.DispatchFacade;
import com.wm.app.b2b.server.dispatcher.trigger.Trigger;
import com.wm.app.b2b.server.dispatcher.wmmessaging.RuntimeConfiguration;
import com.wm.app.b2b.ws.ns.NSFacade;
import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.data.IDataUtil;
import com.wm.lang.ns.NSRecord;

import java.util.Map;

import static com.softwareag.tom.is.pkg.dapp.trigger.DAppListener.IS_DAPP_CONNECTION;
// --- <<IS-END-IMPORTS>> ---

@SuppressWarnings("unused") public final class Admin {
    /**
     * Creates new DApp connection alias if not already existing.
     *
     * @param pipeline The pipeline
     * @throws ServiceException If there is an error during execution of this service
     */
    public static void createConnectionAlias(IData pipeline) throws ServiceException {
        // --- <<IS-START(createConnectionAlias)>> ---
        // @subtype unknown
        // @sigtype java 3.5
        // [o] field:0:required message
        IDataCursor pc = pipeline.getCursor();
        String message;
        try {
            RuntimeConfiguration rt = DispatchFacade.getRuntimeConfiguration();
            if (rt.getConnectionAliasOrNull(IS_DAPP_CONNECTION) != null) {
                message = "Connection alias '" + IS_DAPP_CONNECTION + "' already exists.";
            } else {
                IData input = IDataFactory.create();
                IDataCursor ic = input.getCursor();
                IDataUtil.put(ic, "aliasName", IS_DAPP_CONNECTION);
                IDataUtil.put(ic, "description", "system generated Decentralised Application connection alias");
                IDataUtil.put(ic, "enabled", true);
                IDataUtil.put(ic, "systemGenerated", true);
                ic.destroy();

                rt.createDAppConnectionAliasReference(input);
                message = "Successfully created connection alias '" + IS_DAPP_CONNECTION + "'.";
            }
            IDataUtil.put(pc,"message", message);
        } catch (Exception e) {
            message = "Failed to create DApp connection alias!";
            IDataUtil.put(pc,"message", message);
            throw new ServiceException(e);
        }
        // --- <<IS-END>> ---
    }

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
        // [i] field:0:optional deployedOnly {"false","true"}
        // [o] field:0:required message
        IDataCursor pc = pipeline.getCursor();
        boolean deployedOnly = IDataUtil.getBoolean(pc,"deployedOnly", false);
        String message;
        try {
            Map<String,FlowSvcImpl> functions = Util.instance.getFunctions(deployedOnly);
            for (FlowSvcImpl function : functions.values()) {
                if (NSFacade.getNSNode(function.getNSName().getFullName()) == null) {
                    NSFacade.saveNewNSNode(function);
                } else {
                    NSFacade.updateNSNode(function);
                }
            }
            Map<String,Event> events = Util.instance.getEvents(deployedOnly);
            for (Event event : events.values()) {
                NSRecord nsRecord = event.getPdt();
                if (NSFacade.getNSNode(nsRecord.getNSName().getFullName()) == null) {
                    NSFacade.saveNewNSNode(nsRecord);
                } else {
                    NSFacade.updateNSNode(nsRecord);
                }
                Trigger trigger = event.getTrigger();
                if (NSFacade.getNSNode(trigger.getNSName().getFullName()) == null) {
                    NSFacade.saveNewNSNode(trigger);
                } else {
                    NSFacade.updateNSNode(trigger);
                }
                FlowSvcImpl service = event.getService();
                if (NSFacade.getNSNode(service.getNSName().getFullName()) == null) {
                    NSFacade.saveNewNSNode(service);
                } else {
                    NSFacade.updateNSNode(service);
                }
            }
            message = "Successfully synchronized all contracts to the IS namespace.";
            IDataUtil.put(pc,"message", message);
        } catch (Exception e) {
            message = "Failed to synchronize contracts!";
            IDataUtil.put(pc,"message", message);
            throw new ServiceException(e);
        }
        // --- <<IS-END>> ---
    }

    /**
     * Retrieves the contract-address mappings as persisted in file {@code ${config.location}/contract-addresses.json}.
     *
     * @param pipeline The pipeline
     * @throws ServiceException If there is an error during execution of this service
     */
    public static void loadContractAddresses(IData pipeline) throws ServiceException {
        // --- <<IS-START(loadContractAddresses)>> ---
        // @subtype unknown
        // @sigtype java 3.5
        // [o] record:1:optional contracts
        // [o] - field:0:optional uri
        // [o] - field:0:optional address
        IDataCursor pc = pipeline.getCursor();
        IData[]  contracts;
        try {
            contracts = Util.instance.getContractAddresses();
        } catch (Exception e) {
            throw new ServiceException(e);
        }
        IDataUtil.put(pc,"contracts", contracts);
        // --- <<IS-END>> ---
    }

    /**
     * Deploys the contract to the distributed ledger and stores the contract-address mappings to file {@code ${config.location}/contract-addresses.json} if successful.
     *
     * @param pipeline The pipeline
     * @throws ServiceException If there is an error during execution of this service
     */
    public static void deployContract(IData pipeline) throws ServiceException {
        // --- <<IS-START(deployContract)>> ---
        // @subtype unknown
        // @sigtype java 3.5
        // [i] field:0:required uri
        // [o] field:0:required message
        IDataCursor pc = pipeline.getCursor();
        String uri = IDataUtil.getString(pc,"uri");
        String message;
        try {
            String contractAddress = Util.instance.deployContract(uri);
            Util.instance.storeContractAddress(uri, contractAddress);
            message = "Successfully deployed contract '" + uri + "'.";
            IDataUtil.put(pc,"message", message);
        } catch (Exception e) {
            message = "Failed to deploy contract '" + uri + "'!";
            IDataUtil.put(pc,"message", message);
            throw new ServiceException(e);
        }
        // --- <<IS-END>> ---
    }
}