package com.billing.ot.dto;

import com.billing.enums.PaymentMode;
import com.billing.ot.enums.OTPaymentType;

import lombok.Data;

@Data
public class OTPaymentRequest {
    private Long operationExternalId;
    private Long patientExternalId;
    private OTPaymentType paymentType;
    private PaymentMode paymentMode;
    private Double amount;
    private String referenceNumber;
    private String receivedBy;
    private String notes;
}
