package com.customer.main.exception;

/**
 * Thrown for business rule violations (e.g. age not eligible for home loan).
 */
public class BusinessException extends RuntimeException {
    private final String errorCode;

    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
