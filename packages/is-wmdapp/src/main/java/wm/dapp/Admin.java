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
import com.softwareag.tom.is.pkg.dapp.Util;
import com.softwareag.util.IDataMap;
import com.wm.app.b2b.server.FlowSvcImpl;
import com.wm.app.b2b.server.ServiceException;
import com.wm.app.b2b.ws.ns.NSFacade;
import com.wm.data.IData;
// --- <<IS-END-IMPORTS>> ---

@SuppressWarnings("unused") public final class Admin {
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
        try {
            for (FlowSvcImpl flowSvcImpl : Util.instance.getFunctions().values()) {
                NSFacade.saveNewNSNode(flowSvcImpl);
            }
        } catch (Exception e) {
            throw new ServiceException(e);
        }
        // --- <<IS-END>> ---
    }

    /**
     * Retrieves the contract-address mappings.
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
        IData[]  contracts;
        try {
            contracts = Util.instance.getContractAddresses();
        } catch (Exception e) {
            throw new ServiceException(e);
        }
        new IDataMap(pipeline).put("contracts", contracts);
        // --- <<IS-END>> ---
    }

    /**
     * Deploys the conytract to the distributed ledger.
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
        IDataMap pipe = new IDataMap(pipeline);
        String uri = pipe.getAsString("uri");
        String contractAddress = Util.instance.deployContract(uri);
        String message;
        try {
            Util.instance.storeContractAddresse(uri, contractAddress);
            message = "Successfully deployed contract '" + uri + "'.";
            pipe.put("message", message);
        } catch (Exception e) {
            message = "Failed to deploy contract '" + uri + "'.";
            pipe.put("message", message);
            throw new ServiceException(e);
        }
        // --- <<IS-END>> ---
    }
}