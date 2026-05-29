package com.billing.icu.entity;

import com.billing.model.BillingMaster;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ICU Billing ka master record — OTBillingDetails ki tarah.
 * BillingMaster se linked hai (moduleType = "ICU").
 */
@Entity
@Table(name = "icu_billing_details",
        indexes = {
                @Index(name = "idx_icu_billing_admission", columnList = "icuAdmissionId"),
                @Index(name = "idx_icu_billing_hospital",  columnList = "hospitalExternalId"),
                @Index(name = "idx_icu_billing_patient",   columnList = "patientExternalId"),
                @Index(name = "idx_icu_billing_status",    columnList = "billingStatus")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ICUBillingDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ─── Link to BillingMaster ─────────────────────────────────────
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billing_id", nullable = false)
    private BillingMaster billingMaster;

    // ─── External References ───────────────────────────────────────
    @Column(nullable = false)
    private Long icuAdmissionId;

    private Long hospitalExternalId;
    private Long patientExternalId;

    // ─── Admission Info ────────────────────────────────────────────
    private Long totalDays;
    private LocalDateTime admittedAt;
    private LocalDateTime dischargedAt;

    // ─── Charge Breakup (calculated by Billing service) ───────────
    private Double bedCharge;           // bedChargePerDay * totalDays
    private Double nursingCharge;       // nursingChargePerDay * totalDays
    private Double doctorCharge;        // doctorVisitChargePerDay * totalDays
    private Double monitoringCharge;    // monitoringChargePerDay * totalDays
    private Double oxygenCharge;        // oxygenChargePerDay * totalDays
    private Double admissionCharge;     // one-time

    private Double totalEquipmentCharge;
    private Double totalMedicationCharge;

    // ─── Summary ───────────────────────────────────────────────────
    private Double grossAmount;         // sum of all charges before discount
    private Double totalDiscountAmount;
    private Double totalGstAmount;
    private Double totalAmount;         // final payable
    private Double advancePaid;
    private Double due;                 // totalAmount - advancePaid

    // ─── Status ────────────────────────────────────────────────────
    /** ACTIVE / CLOSED / CANCELLED */
    private String billingStatus;

    // ─── Audit ────────────────────────────────────────────────────
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ─── Child Collections ────────────────────────────────────────
    @OneToMany(mappedBy = "icuBillingDetails",
               cascade = CascadeType.ALL,
               fetch = FetchType.LAZY,
               orphanRemoval = true)
    @Builder.Default
    private List<ICUEquipmentBilling> equipmentCharges = new ArrayList<>();

    @OneToMany(mappedBy = "icuBillingDetails",
               cascade = CascadeType.ALL,
               fetch = FetchType.LAZY,
               orphanRemoval = true)
    @Builder.Default
    private List<ICUMedicationBilling> medicationCharges = new ArrayList<>();

    @OneToMany(mappedBy = "icuBillingDetails",
               cascade = CascadeType.ALL,
               fetch = FetchType.LAZY)
    @Builder.Default
    private List<ICUPayment> payments = new ArrayList<>();

    // ─── Lifecycle ────────────────────────────────────────────────
    @PrePersist
    protected void onCreate() {
        createdAt   = LocalDateTime.now();
        updatedAt   = LocalDateTime.now();
        // Only set defaults if not already set by service (applyCalculation)
        if (billingStatus == null) billingStatus = "ACTIVE";
        if (advancePaid   == null) advancePaid   = 0.0;
        if (totalDiscountAmount == null) totalDiscountAmount = 0.0;
        if (totalGstAmount      == null) totalGstAmount      = 0.0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}