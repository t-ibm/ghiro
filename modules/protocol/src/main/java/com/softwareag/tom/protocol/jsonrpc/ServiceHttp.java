/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.jsonrpc;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON-RPC over HTTP service implementation.
 */
public class ServiceHttp extends ServiceBase {

    private CloseableHttpClient httpClient;

    private final String url;

    public ServiceHttp(String url, CloseableHttpClient httpClient) {
        this.url = url;
        this.httpClient = httpClient;
    }

    public ServiceHttp(String url) {
        this(url, HttpClients.custom().setConnectionManagerShared(true).build());
    }

    @Override public <T extends Response> T send(Request request, Class<T> responseType) throws IOException {

        byte[] payload = objectMapper.writeValueAsBytes(request);

        HttpPost httpPost = new HttpPost(this.url);
        httpPost.setEntity(new ByteArrayEntity(payload));
        Header[] headers = buildHeaders();
        httpPost.setHeaders(headers);

        ResponseHandler<T> responseHandler = getResponseHandler(responseType);
        try {
            return httpClient.execute(httpPost, responseHandler);
        } finally {
            httpClient.close();
        }
    }

    private Header[] buildHeaders() {
        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader("Content-Type", "application/json; charset=UTF-8"));
        return headers.toArray(new Header[0]);
    }

    protected <T extends Response> ResponseHandler<T> getResponseHandler(Class<T> type) {
        return response -> {
            int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();

                if (entity != null) {
                    return objectMapper.readValue(response.getEntity().getContent(), type);
                } else {
                    return null;
                }
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
        };
    }
}