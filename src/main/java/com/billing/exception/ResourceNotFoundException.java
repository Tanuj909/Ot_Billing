package com.billing.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BillingException {

    public ResourceNotFoundException(String message) {
        super(message, ErrorCode.BILL_NOT_FOUND, HttpStatus.NOT_FOUND);
    }
}