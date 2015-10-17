package io.ticofab.cm_android_sdk.library.exceptions;

/**
 * Exception used to signal a connection exception
 */
public class CloudMatchInvalidCredentialException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public CloudMatchInvalidCredentialException() {
        super("The provided credentials are invalid.");
    }
}
