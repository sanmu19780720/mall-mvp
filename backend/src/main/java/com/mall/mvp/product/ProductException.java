package com.mall.mvp.product;

import org.springframework.http.HttpStatus;

/**
 * Signals a product-domain failure that maps directly to an HTTP status and a stable
 * machine-readable {@code error} code (e.g. {@code PRODUCT_NOT_FOUND}). Handled by
 * {@link ProductController}'s exception handler.
 */
public class ProductException extends RuntimeException {

    private final HttpStatus status;
    private final String error;

    public ProductException(HttpStatus status, String error) {
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
