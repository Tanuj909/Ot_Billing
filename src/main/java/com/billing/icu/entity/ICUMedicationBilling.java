package com.billing.icu.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * ICU Medication ka per-medicine billing record.
 * durationDays * dosesPerDay * costPerDose = base amount (before GST/discount).
 *
 * FIX: calculateAmounts() ko @PrePersist / @PreUpdate se HATAYA gaya hai.
 *      Reason: ServiceImpl mein amounts already set hote hain (ya calculateAmounts()
 *              explicitly call hoti hai). @PrePersist mein dobara call se
 *              pre-calculated totalCost overwrite ho jaati thi.
 */
@Entity
@Table(name = "icu_medication_billing",
        indexes = {
                @Index(name = "idx_icu_med_billing",  columnList = "icu_billing_id"),
                @Index(name = "idx_icu_med_external", columnList = "medicationExternalId")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ICUMedicationBilling {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "icu_billing_id", nullable = false)
    private ICUBillingDetails icuBillingDetails;

    private Long    medicationExternalId;   // ICU system ka ICUMedication.id
    private String  medicineName;

    /** Kitne din medication chali */
    private Long    durationDays;

    /** Ek din mein kitni doses (OD=1, BD=2, TDS=3, QID=4) */
    private Integer dosesPerDay;

    /** Har dose ki unit cost */
    private Double  costPerDose;

    private Double discountPercent;
    private Double discountAmount;
    private Double priceAfterDiscount;

    private Double gstPercent;
    private Double gstAmount;
    private Double totalAmount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ─── Lifecycle ────────────────────────────────────────────────
    // FIX: calculateAmounts() yahan NAHI call karein.
    //      ServiceImpl already amounts set kar deta hai build() ke baad.
    //      Yahan call karne se pre-calculated values overwrite ho jaati thi.
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ─── Calculation helper ───────────────────────────────────────
    /**
     * ServiceImpl explicitly call karta hai jab ICU ne totalCost nahi bheja.
     * Direct set karne ke baad (pre-calculated case) yeh call NAHI hoti.
     */
    public void calculateAmounts() {
        if (durationDays == null || dosesPerDay == null || costPerDose == null) return;

        double baseAmount        = durationDays * dosesPerDay * costPerDose;
        double effectiveDiscount = discountPercent != null ? discountPercent : 0.0;
        this.discountAmount      = baseAmount * effectiveDiscount / 100;
        this.priceAfterDiscount  = baseAmount - discountAmount;

        double effectiveGst  = gstPercent != null ? gstPercent : 0.0;
        this.gstAmount       = priceAfterDiscount * effectiveGst / 100;
        this.totalAmount     = priceAfterDiscount + gstAmount;
    }
}