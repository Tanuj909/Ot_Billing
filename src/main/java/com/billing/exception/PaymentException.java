package com.billing.exception;

import org.springframework.http.HttpStatus;

public class PaymentException extends BillingException {

    public PaymentException(String message) {
        super(
                message,
                ErrorCode.INVALID_PAYMENT,
                HttpStatus.ALREADY_REPORTED
        );
    }
}