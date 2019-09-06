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
import com.wm.lang.ns.NSName;
import com.wm.msg.ConditionFactory;
import com.wm.msg.ICondition;

public final class Condition {
    private String type;
    private String name;
    private NSName pdt;
    private NSName svc;
    private String filter;

    private Condition(String type, NSName pdt, NSName svc, String filter) {
        this.type = type;
        this.name = "Condition " + pdt.getNodeName().toString();
        this.svc = svc;
        this.pdt = pdt;
        this.filter = filter;
    }

    public static Condition create(NSName pdt, NSName svc, String filter) {
        return new Condition(ConditionFactory.SIMPLE, pdt, svc, filter);
    }

    public static Condition create(NSName pdt, NSName svc) {
        return create(pdt, svc, null);
    }

    public ICondition asCondition() {
        return ConditionFactory.getInstance(type).create(getData());
    }

    public IData asIData() {
        return IDataFactory.create(new Object[][]{
            {"type", type},
            {"data", getData()},
        });
    }

    private IData getData() {
        IData messageTypeFilterPair = IDataFactory.create(new Object[][]{
            {"messageType", pdt.getFullName()},
            {"filter", filter},
        });
        return IDataFactory.create(new Object[][]{
            {"conditionName", name},
            {"serviceName", svc.getFullName()},
            {"messageTypeFilterPair", messageTypeFilterPair},
        });
    }
}