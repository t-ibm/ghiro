/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.integration.protocol

import com.softwareag.tom.extension.Node
import groovy.json.JsonOutput
import groovyx.net.http.ContentType
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Shared
import spock.lang.Specification

/**
 * A base specification providing support for common REST client handling.
 */
abstract class RestClientBaseSpecification extends Specification {
    protected static final Logger logger = LoggerFactory.getLogger(RestClientBaseSpecification.class)

    @Shared @Node protected ConfigObject config
    @Shared protected RESTClient client
    @Shared protected HttpResponseDecorator resp

    protected HttpResponseDecorator send(String httpRequest) {
        try {
            println ">>> $httpRequest"
            resp = client.get(path: httpRequest, contentType: ContentType.JSON.toString()) as HttpResponseDecorator
            assertResponse()
        } catch (IOException e) {
            logger.error("Unable to send the request, got exception: " + e)
        }
        resp
    }

    protected HttpResponseDecorator send(Map jsonRequest) {
        String request = JsonOutput.toJson(jsonRequest)
        try {
            println ">>> $request"
            resp = client.post(contentType: ContentType.JSON.toString(), body: request) as HttpResponseDecorator
            assertResponse()
        } catch (IOException e) {
            logger.error("Unable to send the request, got exception: " + e)
        }
        resp
    }

    private assertResponse() {
        assert resp.success
        assert resp.status == 200
        assert (resp.contentType == ContentType.JSON.toString() || resp.contentType == ContentType.TEXT.toString())
        String response = JsonOutput.toJson(resp.data)
        println "<<< $response\n"
    }
}
