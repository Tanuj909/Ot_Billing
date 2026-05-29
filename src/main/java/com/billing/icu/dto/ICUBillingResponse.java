package com.billing.icu.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Billing microservice → caller ko yeh DTO return karta hai.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ICUBillingResponse {

    private Long billingId;                 // ICUBillingDetails.id
    private Long billingMasterId;           // BillingMaster.id
    private Long icuAdmissionId;
    private Long hospitalId;
    private Long patientId;

    // ─── Duration ────────────────────────────────────────────────
    private Long totalDays;
    private String admittedAt;
    private String dischargedAt;

    // ─── Charge Breakup ───────────────────────────────────────────
    private Double bedCharge;
    private Double nursingCharge;
    private Double doctorCharge;
    private Double monitoringCharge;
    private Double oxygenCharge;
    private Double admissionCharge;
    private Double totalEquipmentCharge;
    private Double totalMedicationCharge;

    // ─── Summary ─────────────────────────────────────────────────
    private Double grossAmount;
    private Double totalDiscountAmount;
    private Double totalGstAmount;
    private Double totalAmount;
    private Double advancePaid;
    private Double due;

    // ─── Status ──────────────────────────────────────────────────
    private String billingStatus;       // ACTIVE / CLOSED / CANCELLED

    // ─── Line Items ──────────────────────────────────────────────
    private List<EquipmentLineItem> equipmentCharges;
    private List<MedicationLineItem> medicationCharges;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EquipmentLineItem {
        private Long equipmentAssignmentId;
        private Long equipmentExternalId;
        private String equipmentName;
        private Double totalHours;
        private Double costPerHour;
        private Double discountPercent;
        private Double discountAmount;
        private Double gstPercent;
        private Double gstAmount;
        private Double totalAmount;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MedicationLineItem {
        private Long medicationExternalId;
        private String medicineName;
        private Long durationDays;
        private Integer dosesPerDay;
        private Double costPerDose;
        private Double discountPercent;
        private Double discountAmount;
        private Double gstPercent;
        private Double gstAmount;
        private Double totalAmount;
    }
}