package dev.exterminate.oauthlite.util;

public class OAuthException extends Exception {
    public OAuthException(String message) {
        super(message);
    }

    public OAuthException(Throwable cause) {
        super(cause);
    }
}
