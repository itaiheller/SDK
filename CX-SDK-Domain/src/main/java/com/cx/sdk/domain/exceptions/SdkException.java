package com.cx.sdk.domain.exceptions;

/**
 * Created by ehuds on 2/28/2017.
 */
public class SdkException extends RuntimeException {
    public SdkException(String message) {
        super(message);
    }

    public SdkException(String message, Exception exception) {
        super(message, exception);
    }

    public SdkException(Exception e) {
        this(e.getMessage());
    }
}
