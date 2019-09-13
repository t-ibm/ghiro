/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2018 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or
 * its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.is.pkg.dapp

import com.wm.app.b2b.server.Package
import com.wm.app.b2b.server.PackageStore
import com.wm.app.b2b.server.dispatcher.wmmessaging.ConnectionAlias
import com.wm.app.b2b.server.dispatcher.wmmessaging.RuntimeConfiguration
import com.wm.app.b2b.server.ns.Namespace
import com.wm.lang.ns.NSName
import com.wm.lang.ns.NSRecord
import spock.lang.Specification

/**
 * A base specification providing common IS messaging support.
 */
class RuntimeBaseSpecification extends Specification {
    def setupSpec() {
        // Inject IS package store into IS package
        Package pkg = Mock(Package)
        pkg.getStore() >> {
            Mock(PackageStore)
        }
        // Add envelope
        Namespace ns = Namespace.current()
        NSName envNsName = NSName.create("pub.publish:envelope")
        NSRecord envNsRecord = new NSRecord(ns, envNsName.getFullName(), NSRecord.DIM_SCALAR)
        envNsRecord.setNSName(envNsName)
        envNsRecord.setPackage(pkg)
        ns.putNode(envNsRecord)
        // Inject DApp connection into connection alias
        ConnectionAlias connectionAlias = Mock(ConnectionAlias)
        connectionAlias.isMessagingType() >> {
            true
        }
        connectionAlias.getType() >> {
            ConnectionAlias.TYPE.DAPP
        }
        // Inject connection alias into runtime configuration
        RuntimeConfiguration runtimeConfiguration = Mock(RuntimeConfiguration)
        runtimeConfiguration.getConnectionAlias(_ as String) >> {
            connectionAlias
        }
        // Set runtime configuration
        Util.instance().rt = runtimeConfiguration
    }
}
