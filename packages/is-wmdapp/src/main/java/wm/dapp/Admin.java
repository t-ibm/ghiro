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
import com.wm.app.b2b.ws.codegen.FlowGenUtil;
import com.wm.app.b2b.ws.ns.NSFacade;
import com.wm.data.IData;
import com.wm.lang.flow.FlowInvoke;
import com.wm.lang.ns.NSName;
import com.wm.lang.ns.NSSignature;
import com.wm.util.Name;

import java.io.IOException;
import java.util.Map;
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
        try {
            Util.create().syncContracts();
        } catch (Exception e) {
            throw new ServiceException(e);
        }
        // --- <<IS-END>> ---
    }
}