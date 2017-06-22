/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.integration.endpoint

import com.softwareag.tom.extension.Node
import groovyx.net.http.ContentType
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient
import spock.lang.Shared
import spock.lang.Specification

class BurrowTest extends Specification {

    @Shared @Node protected ConfigObject config

    public "test 'client version'"() {
        given: 'a REST client'
        RESTClient client = new RESTClient("http://${config.node.host.ip}:${config.node.host.port}")

        when: 'we make a get request'
        HttpResponseDecorator resp = client.get(path: '/network/client_version', contentType: ContentType.JSON.toString()) as HttpResponseDecorator

        then: 'we receive a valid response'
        resp.success
        resp.status == 200
        resp.contentType == ContentType.TEXT.toString()
        println "response payload - $resp.data"
        resp.data == ['client_version': '0.8.0']
    }
}