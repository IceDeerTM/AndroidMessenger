package com.deerdevelop.androidmessenger.model;

/**
 * This class is used to store token keys
 */
public class StorageToken {
    private static String tokenDevice, tokenJWT = "";

    public static String getTokenDevice() {
        return tokenDevice;
    }

    public static void setTokenDevice(String tokenDevice) {
        StorageToken.tokenDevice = tokenDevice;
    }

    public static String getTokenJWT() {
        return tokenJWT;
    }

    public static void setTokenJWT(String tokenJWT) {
        StorageToken.tokenJWT = tokenJWT;
    }
}
