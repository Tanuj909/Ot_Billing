package com.billing.laboratory.dto;

import lombok.Data;

@Data
public class LabDiscountResponse {

    private Double totalAmount;
    private Double due;
    private Double discountAmount;
    private Double discountPercentage;
    private String paymentStatus;
}
