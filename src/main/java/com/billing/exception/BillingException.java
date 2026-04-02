package com.billing.exception;

import org.springframework.http.HttpStatus;

public class BillingException extends RuntimeException {

    private final ErrorCode errorCode;
    private final HttpStatus status;

    public BillingException(String message, ErrorCode errorCode, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }
}