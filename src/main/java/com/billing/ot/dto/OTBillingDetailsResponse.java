package com.billing.ot.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OTBillingDetailsResponse {
    private Long id;
    private Long billingMasterId;
    private Long operationExternalId;
    private String operationReference;
    private Long hospitalExternalId;
    private Long patientExternalId;

    // Totals
    private Double totalStaffCharges;
    private Double totalRoomCharges;
    private Double totalItemCharges;
    private Double totalDiscountAmount;
    private Double totalGstAmount;
    private Double grossAmount;
    private Double totalAmount;
    private Double advancePaid;
    private Double due;

    private String billingStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}