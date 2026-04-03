package com.billing.ot.dto;

import java.util.List;

import com.billing.enums.PaymentStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OTPaymentHistoryResponse {
    private Long operationExternalId;
    private String operationReference;
    private Double totalAmount;
    private Double totalPaid;
    private Double totalRefunded;
    private Double due;
    private PaymentStatus billingPaymentStatus;
    private List<OTPaymentResponse> payments;
    private List<OTRefundResponse> refunds;
}