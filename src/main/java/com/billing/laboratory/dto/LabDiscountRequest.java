package com.billing.laboratory.dto;

import lombok.Data;

@Data
public class LabDiscountRequest {

    private Long labBillingId;

    // Either percentage OR amount (not both mandatory)
    private Double discountPercentage;
    private Double discountAmount;

    private String reason;
}
