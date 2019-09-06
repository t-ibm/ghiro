/*
 * Copyright (c) 2017 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its
 * subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in
 * your License Agreement with Software AG.
 */
package com.softwareag.tom.util;

import org.bouncycastle.jcajce.provider.digest.Keccak;

/**
 * The Keccak (SHA-3) hash helper class.
 */
public final class Hash {
    private Hash() {}
    /**
     * Keccak (SHA-3) hash.
     *
     * @param data hex encoded string
     * @return hash value as hex encoded string
     */
    public static String sha3(String data) {

        byte[] bytes = HexValueBase.toByteArray(data);
        byte[] result = sha3(bytes);
        return HexValueBase.toString(result);
    }
    /**
     * Keccak (SHA-3) hash.
     *
     * @param data binary encoded data
     * @return hash value
     */
    private static byte[] sha3(byte[] data) {
        return sha3(data, 0, data.length);
    }
    private static byte[] sha3(byte[] input, int offset, int length) {
        Keccak.DigestKeccak kecc = new Keccak.Digest256();
        kecc.update(input, offset, length);
        return kecc.digest();
    }
}