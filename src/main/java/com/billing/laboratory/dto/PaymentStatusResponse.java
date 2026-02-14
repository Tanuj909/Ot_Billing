package com.billing.laboratory.dto;

import com.billing.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentStatusResponse {

    private Long billingId;
    private Long labOrderId;

    private PaymentStatus paymentStatus;

    private Double totalAmount;
    private Double totalPaid;
    private Double dueAmount;

    private String billingStatus; // ACTIVE / CANCELLED
}
