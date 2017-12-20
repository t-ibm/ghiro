/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.is.pkg.dapp;

import com.wm.util.B2BListResourceBundle;

public class DAppMsgBundle extends B2BListResourceBundle {

    static final int FAC = 60;//JournalLogger.FAC_DAPP; //TODO

    // Keys
    public static final int DAPP_SERVICES_MKDIRS = 1;

    // Values
    private static final Object [][] contents = new Object[][]{
            {FAC + "." + DAPP_SERVICES_MKDIRS, "Node directory \"{0}\" already exists or creation failed."}
    };

    @Override protected Object[][] getContents() {
        return contents;
    }
}