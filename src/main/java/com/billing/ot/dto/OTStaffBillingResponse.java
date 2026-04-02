package com.billing.ot.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OTStaffBillingResponse {
    private Long id;
    private Long otBillingDetailsId;
    private Long staffExternalId;
    private String staffName;
    private String staffRole;
    private Double fees;
    private Double discountPercent;
    private Double discountAmount;
    private Double priceAfterDiscount;
    private Double gstPercent;
    private Double gstAmount;
    private Double totalAmount;
    private LocalDateTime serviceAddedAt;
    private LocalDateTime createdAt;
}