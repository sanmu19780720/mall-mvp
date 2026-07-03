package com.mall.mvp.user;

import org.springframework.http.HttpStatus;

/**
 * Signals a user-domain failure (e.g. validation or a uniqueness conflict) that maps
 * directly to an HTTP status and a stable machine-readable {@code error} code
 * (e.g. {@code USERNAME_EXISTS}). Handled by {@link UserController}'s exception handler.
 */
public class UserException extends RuntimeException {

    private final HttpStatus status;
    private final String error;

    public UserException(HttpStatus status, String error) {
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
