package io.ticofab.cm_android_sdk.library.exceptions;

/**
 * Exception used to signal a connection exception
 * 
 * @author @ticofab
 * 
 */
public class CloudMatchConnectionException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public CloudMatchConnectionException() {
        super("Something is going wrong on the server.");
    }
}
