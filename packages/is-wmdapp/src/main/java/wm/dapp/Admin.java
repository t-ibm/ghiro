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
import com.softwareag.tom.contract.ContractRegistry;
import com.softwareag.tom.contract.SolidityLocationFileSystem;
import com.wm.app.b2b.server.ServiceException;
import com.wm.data.IData;

import java.io.IOException;
// --- <<IS-END-IMPORTS>> ---

public final class Admin {
	/**
	 * Determines the current date.
	 * 
	 * @param	pipeline The pipeline
	 * @throws	ServiceException If there is an error during execution of this service
	 */
	public static void getCurrentDate (IData pipeline) throws ServiceException {
		// --- <<IS-START(getCurrentDate)>> ---
		// @subtype unknown
		// @sigtype java 3.5
		try {
			ContractRegistry contractRegistry = ContractRegistry.build(new SolidityLocationFileSystem("tbd"));
		} catch (IOException e) {
			throw new ServiceException(e);
		}
		// --- <<IS-END>> ---
	}
}