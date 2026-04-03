package com.billing.ot.dto;

import com.billing.enums.PaymentMode;

import lombok.Data;

@Data
public class BillingMasterRequest {
    private Long hospitalExternalId;
    private Long patientExternalId;
    private Long otOperationId;
    private String moduleType;          // "OT", "LAB", "PHARMACY"
    private PaymentMode paymentMode;
    private String advancePaymentMode;
}
