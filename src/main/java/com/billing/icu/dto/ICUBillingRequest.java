package com.billing.icu.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ICU Microservice → Billing Microservice ko yeh DTO bhejta hai.
 * Saari raw data hoti hai — calculation Billing side par hogi.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ICUBillingRequest {

    // ─── Identifiers ───────────────────────────────────────────────
    private Long icuAdmissionId;
    private Long hospitalId;
    private Long patientId;

    // ─── Admission Duration ────────────────────────────────────────
    /** Total days patient admitted raha (ICU calculate karke bhejta hai) */
    private Long totalDays;

    /** Admission start & end timestamps (ISO string) — audit ke liye */
    private String admittedAt;
    private String dischargedAt;        // null if still admitted

    // ─── Daily Charges (per-day rates from ICUUnit) ────────────────
    private Double bedChargePerDay;
    private Double nursingChargePerDay;
    private Double doctorVisitChargePerDay;
    private Double monitoringChargePerDay;
    private Double oxygenChargePerDay;

    /** One-time charge at admission */
    private Double admissionCharge;

    // ─── Equipment Usage ───────────────────────────────────────────
    private List<EquipmentUsageItem> equipmentUsages;

    // ─── Medication Usage ──────────────────────────────────────────
    private List<MedicationUsageItem> medicationUsages;

    // ──────────────────────────────────────────────────────────────
    // Inner DTOs
    // ──────────────────────────────────────────────────────────────

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EquipmentUsageItem {
        private Long equipmentAssignmentId;
        private Long equipmentId;
        private String equipmentName;

        /** Total hours equipment use hua (ICU calculates: removedAt - assignedAt) */
        private Double totalHours;

        /** Per hour rate */
        private Double costPerHour;

        /**
         * Agar ICU ne already totalCost set kiya ho (pre-calculated),
         * toh Billing use karega; warna totalHours * costPerHour.
         */
        private Double totalCost;

        private String status;          // IN_USE / REMOVED
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MedicationUsageItem {
        private Long medicationId;
        private String medicineName;

        /** Kitne din medication chali */
        private Long durationDays;

        /** Ek din mein kitni doses (OD=1, BD=2, TDS=3, QID=4) */
        private Integer dosesPerDay;

        /** Har ek dose ki cost */
        private Double costPerDose;

        /** Pre-calculated agar ICU ne bheja ho (optional) */
        private Double totalCost;
    }
}