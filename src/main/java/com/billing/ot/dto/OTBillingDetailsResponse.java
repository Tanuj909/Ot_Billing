package com.billing.ot.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class OTBillingDetailsResponse {

    private Long   id;
    private Long   billingMasterId;
    private Long   operationExternalId;
    private String operationReference;
    private Long   hospitalExternalId;
    private Long   patientExternalId;

    // ── Totals ─────────────────────────────────────────────────────────────
    private Double totalStaffCharges;
    private Double totalRoomCharges;
    private Double totalRecoveryCharges;
    private Double totalItemCharges;
    private Double totalDoctorVisitCharges; // ✅ BUG FIX — pehle missing tha

    private Double totalDiscountAmount;
    private Double totalGstAmount;
    private Double grossAmount;
    private Double totalAmount;
    private Double advancePaid;
    private Double due;

    private String        billingStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}