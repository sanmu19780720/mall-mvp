package com.mall.mvp.auth;

import org.springframework.http.HttpStatus;

/**
 * Signals an auth failure that maps directly to an HTTP status and a stable
 * machine-readable {@code error} code (e.g. {@code INVALID_CREDENTIALS}).
 * Handled by {@link AuthController}'s exception handler.
 */
public class AuthException extends RuntimeException {

    private final HttpStatus status;
    private final String error;

    public AuthException(HttpStatus status, String error) {
        super(error);
        this.status = status;
        this.error = error;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }
}
