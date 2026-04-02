package com.billing.ot.dto;

import com.billing.enums.PaymentMode;
import com.billing.enums.PaymentStatus;

import lombok.Data;

@Data
public class BillingMasterUpdateRequest {
    private Double totalAmount;
    private PaymentStatus paymentStatus;
    private PaymentMode paymentMode;
}
