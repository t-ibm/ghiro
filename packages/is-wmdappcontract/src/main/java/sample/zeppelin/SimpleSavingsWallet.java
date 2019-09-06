/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2018 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or
 * its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package sample.zeppelin;
// --- <<B2B-START-IMPORTS>> ---
import com.softwareag.tom.util.HexValueBase;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.data.IDataUtil;
// --- <<B2B-END-IMPORTS>> ---

@SuppressWarnings("unused") public final class SimpleSavingsWallet {
    // --- <<IS-START-SHARED>> ---
    // --- <<IS-END-SHARED>> ---
    private SimpleSavingsWallet() {}
    public static void sendTo(IData pipeline) throws ServiceException {
        // --- <<B2B-START(sendTo)>> ---
        // @sigtype java 3.5
        // [i] field:0:required payee
        // [i] field:0:required amount
        IDataCursor cursor = pipeline.getCursor();
        String payee = IDataUtil.getString(cursor,"payee");
        String amount = IDataUtil.getString(cursor,"amount");
        Object[][] input = {
                { "payee", HexValueBase.toBigInteger(payee) },
                { "amount", HexValueBase.toBigInteger(amount) },
        };
        try {
            IData output = Service.doInvoke("zeppelin.examples.SimpleSavingsWallet", "sendToReq", IDataFactory.create(input));
            IDataUtil.merge(pipeline, output);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(e);
        }
        // --- <<B2B-END>> ---
    }
}
