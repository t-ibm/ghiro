/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2018 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or
 * its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.is.pkg.dapp;

import com.wm.app.b2b.server.dispatcher.trigger.Trigger;
import com.wm.lang.ns.NSRecord;

import java.util.Objects;

public class Event {
    private Trigger trigger;
    private NSRecord pdt;

    private Event(Trigger trigger, NSRecord pdt) {
        this.trigger = trigger;
        this.pdt = pdt;
    }

    public static Event create(Trigger trigger, NSRecord pdt) {
        return new Event(trigger, pdt);
    }

    public Trigger getTrigger() { return trigger; }
    public NSRecord getPdt() { return pdt; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event)o;
        return Objects.equals(trigger, event.trigger) && Objects.equals(pdt, event.pdt);
    }
    @Override public int hashCode() {
        return Objects.hash(trigger, pdt);
    }
}