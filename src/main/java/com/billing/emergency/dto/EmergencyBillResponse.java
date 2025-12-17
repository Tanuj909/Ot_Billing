package com.billing.emergency.dto;

import lombok.Data;

@Data
public class EmergencyBillResponse {
    private Long billingId;
    private Long emergencyBillingDetailsId;
    private Long emergencyId;

    private Double totalChargesBeforeDiscount;
    private Double discountPercentage;
    private Double discountAmount;
    private Double totalAfterDiscount;
    private Double advancePaid;
    private Double due;

    // Individual charges (for confirmation)
    private Double doctorFees;
    private Double monitoringCharges;
    private Double nursingCharges;
    private Double emergencyConsumable;
    private Double roomCharges;

    private String message = "Emergency bill generated successfully";
}