package com.billing.laboratory.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RefundStatusResponse {

    private Long refundId;
    private Long labOrderId;
    private Long billingId;

    private Double refundAmount;
    private String refundStatus; // INITIATED / COMPLETED / FAILED

    private String reason;
    private LocalDateTime createdAt;
    private LocalDateTime refundedAt;
}

