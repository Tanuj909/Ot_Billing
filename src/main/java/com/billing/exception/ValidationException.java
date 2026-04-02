package com.billing.exception;

import org.springframework.http.HttpStatus;

public class ValidationException extends BillingException {

    public ValidationException(String message) {
        super(message, ErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST);
    }
}