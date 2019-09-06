/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.protocol.jsonrpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.protobuf.ByteString;
import com.softwareag.tom.protocol.util.HexValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;

/**
 * JSON-RPC base request implementation.
 * @param <P> The parameter type
 * @param <R> The expected response type for this request
 */
public abstract class Request<P, R extends Response> {
    private static final long DEFAULT_CORRELATION_ID = 1;
    protected static final String JSONRPC_VERSION = "2.0";

    protected static final Logger logger = LoggerFactory.getLogger(Request.class);

    private Service jsonRpcService;

    @JsonProperty("jsonrpc") protected String jsonrpc = JSONRPC_VERSION;
    @JsonProperty("method") protected String method;
    @JsonProperty("params") protected P params;
    @JsonProperty("id") protected String id;

    public Request(Service jsonRpcService, String method, P params, long id) {
        this.jsonRpcService = jsonRpcService;
        this.method = method;
        this.params = params;
        this.id = Long.toString(id);
    }

    public Request(Service jsonRpcService, String method, P params) {
        this(jsonRpcService, method, params, DEFAULT_CORRELATION_ID);
    }

    private Class<R> getResponseType() {
        ParameterizedType t = getParameterizedType(getClass());
        @SuppressWarnings("unchecked") Class<R> classOfT = (Class<R>) t.getActualTypeArguments()[1];
        try {
            classOfT.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            logger.warn("Unable to instantiate response type, got exception: " + e);
        }
        return classOfT;
    }

    private static ParameterizedType getParameterizedType(Class<?> c) {
        if (Request.class.equals(c.getSuperclass())) {
            return (ParameterizedType) c.getGenericSuperclass();
        }
        return getParameterizedType(c.getSuperclass());
    }

    protected static String validate(ByteString immutableByteArray) {
        if (immutableByteArray == null || immutableByteArray.size() == 0) {
            return "";
        } else if (immutableByteArray.size() != 20 * 2 + 2) {
            logger.warn("Address size is {} bytes while it should be 20.", immutableByteArray.size() / 2 - 2);
        } else if (!immutableByteArray.isValidUtf8()) {
            logger.warn("Address is not a valid UTF-8 encoded string.");
        } else {
            return HexValue.stripPrefix(immutableByteArray);
        }
        return "";
    }

    public R send() throws IOException {
        return jsonRpcService.send(this, getResponseType());
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Request<?, ?> request = (Request<?, ?>) o;

        if (jsonrpc != null ? !jsonrpc.equals(request.jsonrpc) : request.jsonrpc != null) return false;
        if (method != null ? !method.equals(request.method) : request.method != null) return false;
        if (params != null ? !params.equals(request.params) : request.params != null) return false;
        return id != null ? id.equals(request.id) : request.id == null;

    }

    @Override public int hashCode() {
        int result = jsonrpc != null ? jsonrpc.hashCode() : 0;
        result = 31 * result + (method != null ? method.hashCode() : 0);
        result = 31 * result + (params != null ? params.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }
}