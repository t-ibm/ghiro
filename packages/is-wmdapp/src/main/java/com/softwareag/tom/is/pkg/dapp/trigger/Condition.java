/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2018 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or
 * its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.is.pkg.dapp.trigger;

import com.wm.data.IData;
import com.wm.data.IDataFactory;
import com.wm.msg.ConditionFactory;
import com.wm.msg.ICondition;

public class Condition {
    private String pdtName;
    private String svcName;
    private String filter;

    public Condition(String pdtName, String svcName, String filter) {
        this.pdtName = pdtName;
        this.svcName = svcName;
        this.filter = filter;
    }

    public ICondition asSimpleCondition() {
        IData messageTypeFilterPair = IDataFactory.create(new Object[][]{
            {"messageType", pdtName},
            {"filter", filter},
        });
        IData data = IDataFactory.create(new Object[][]{
            {"messageTypeFilterPair", messageTypeFilterPair},
            {"filter", filter},
        });
        ICondition condition = ConditionFactory.getInstance(ConditionFactory.SIMPLE).create(data);
        condition.setServiceName(svcName);
        return condition;
    }
}