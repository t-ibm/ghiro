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
package pub.my;

// --- <<IS-START-IMPORTS>> ---
import com.wm.app.b2b.server.ServiceException;
import com.wm.data.IData;
import com.wm.data.IDataCursor;

import java.util.Date;
// --- <<IS-END-IMPORTS>> ---

public final class Application {
	/**
	 * Determines the current date.
	 * 
	 * @param	pipeline The pipeline
	 * @throws	ServiceException If there is an error during execution of this service
	 */
	public static void getCurrentDate (IData pipeline) throws ServiceException
	{
		// --- <<IS-START(getCurrentDate)>> ---
		// @subtype unknown
		// @sigtype java 3.5
		// [o] field:0:required date
		Date outDate = new Date();
		
		IDataCursor pipelineCursor = pipeline.getCursor();
		mergeOutput(pipelineCursor, "date", outDate);
		pipelineCursor.destroy();
		// --- <<IS-END>> ---
	}

	// --- <<IS-START-SHARED>> ---
	private static void mergeOutput(IDataCursor id, String key, Object value)
	{
	    if (id.first(key)) {
	        id.setValue(value);
	    } else {
	        id.last();
	        id.insertAfter(key, value);
	    }
	}
	// --- <<IS-END-SHARED>> ---
}