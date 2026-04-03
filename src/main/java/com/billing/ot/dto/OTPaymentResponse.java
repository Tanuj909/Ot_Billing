package com.billing.ot.dto;

import java.time.LocalDateTime;
import com.billing.enums.PaymentMode;
import com.billing.ot.enums.OTPaymentStatus;
import com.billing.ot.enums.OTPaymentType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OTPaymentResponse {
    private Long id;
    private Long otBillingDetailsId;
    private Long patientExternalId;
    private OTPaymentType paymentType;
    private PaymentMode paymentMode;
    private Double amount;
    private String referenceNumber;
    private String receivedBy;
    private OTPaymentStatus status;
    private String notes;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}
