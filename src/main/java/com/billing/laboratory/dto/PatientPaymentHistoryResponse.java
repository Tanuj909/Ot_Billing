package com.billing.laboratory.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PatientPaymentHistoryResponse {

    private Long paymentId;
    private Long labOrderId;
    private Long billingId;

    private Double amount;
    private String paymentMode;
    private String referenceNumber;

    private LocalDateTime paidAt;
}
