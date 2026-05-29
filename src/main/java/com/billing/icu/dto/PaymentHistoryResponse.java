package com.billing.icu.dto;

import com.billing.enums.PaymentMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentHistoryResponse {

    private Long paymentId;

    private Long billingMasterId;

    private Double amount;

    private PaymentMode paymentMode;

    private String referenceNumber;

    private String receivedBy;

    private LocalDateTime paidAt;
}