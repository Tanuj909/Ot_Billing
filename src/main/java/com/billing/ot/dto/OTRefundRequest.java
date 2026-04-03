package com.billing.ot.dto;

import com.billing.enums.PaymentMode;

import lombok.Data;

@Data
public class OTRefundRequest {
    private Long operationExternalId;
    private Long paymentId;
    private Double refundAmount;
    private String reason;
    private PaymentMode refundMode;
    private String referenceNumber;
    private String processedBy;
}