package com.revolut.model;

/**
 * Created by monster on 12.07.17.
 */
public enum ResponseCode {
    OK(0), ERROR(1);

    private ResponseCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    private int statusCode;
}
