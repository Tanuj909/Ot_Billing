package com.billing.exception;

import org.springframework.http.HttpStatus;

public class StatusException extends BillingException {

    public StatusException(String message) {
        super(message, ErrorCode.INACTIVE, HttpStatus.BAD_REQUEST);
    }
}
