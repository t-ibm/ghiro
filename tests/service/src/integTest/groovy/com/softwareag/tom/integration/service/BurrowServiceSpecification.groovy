/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2019 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA,
 * USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically
 * provided for in your License Agreement with Software AG.
 */
package com.softwareag.tom.integration.service

import com.google.protobuf.ByteString
import com.softwareag.tom.extension.Node
import com.softwareag.tom.protocol.BurrowService
import com.softwareag.tom.protocol.api.BurrowQuery
import com.softwareag.tom.protocol.grpc.ServiceQuery
import com.softwareag.tom.protocol.util.HexValue
import org.hyperledger.burrow.Acm
import org.hyperledger.burrow.rpc.RpcQuery
import spock.lang.Shared
import spock.lang.Specification

/**
 * System under specification: {@link BurrowService}.
 * @author tglaeser
 */
class BurrowServiceSpecification extends Specification {

    @Shared @Node protected ConfigObject config
    @Shared protected BurrowQuery burrowQuery

    def setup() {
        burrowQuery = BurrowService.query(new ServiceQuery(config.node.host.ip, config.node.host.grpc.port))
    }

    def cleanup() {
        burrowQuery.getService().shutdown()
    }

    def "test 'burrow.query.GetAccount' service"() {
        when: 'we make a get request'
        String address = "0x9505e4785ff66e23d8b1ecb47a1e49aa01d81c19"
        ByteString byteString = ByteString.copyFrom(HexValue.toByteArray(address))
        RpcQuery.GetAccountParam request = RpcQuery.GetAccountParam.newBuilder().setAddress(byteString).build()
        Acm.Account response = burrowQuery.getAccount(request)
        println ">>> $request.descriptorForType.fullName....$request"
        println "<<< $response.descriptorForType.fullName...$response"

        then: 'a valid response is received'
        HexValue.toString(response.getAddress().toByteArray()) == address
        response.getPermissions().getBase().getPerms() == 262143
        response.getPermissions().getBase().getSetBit() == 262143
    }
}