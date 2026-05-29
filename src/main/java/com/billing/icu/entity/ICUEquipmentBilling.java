package com.billing.icu.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * ICU Equipment ka per-assignment billing record.
 * OTItemBilling ki tarah — har equipment assignment ka alag row.
 *
 * FIX: calculateAmounts() ko @PrePersist / @PreUpdate se HATAYA gaya hai.
 *      Reason: ServiceImpl mein amounts already set hote hain (ya calculateAmounts()
 *              explicitly call hoti hai). @PrePersist mein dobara call se
 *              pre-calculated totalCost overwrite ho jaati thi.
 */
@Entity
@Table(name = "icu_equipment_billing",
        indexes = {
                @Index(name = "idx_icu_eq_billing",  columnList = "icu_billing_id"),
                @Index(name = "idx_icu_eq_external", columnList = "equipmentAssignmentId")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ICUEquipmentBilling {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "icu_billing_id", nullable = false)
    private ICUBillingDetails icuBillingDetails;

    private Long   equipmentAssignmentId;   // ICU system ka EquipmentAssignment.id
    private Long   equipmentExternalId;     // ICU system ka Equipment.id
    private String equipmentName;

    private Double totalHours;
    private Double costPerHour;

    private Double discountPercent;
    private Double discountAmount;
    private Double priceAfterDiscount;

    private Double gstPercent;
    private Double gstAmount;
    private Double totalAmount;

    /** IN_USE / REMOVED */
    private String status;

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
        if (totalHours == null || costPerHour == null) return;

        double baseAmount        = totalHours * costPerHour;
        double effectiveDiscount = discountPercent != null ? discountPercent : 0.0;
        this.discountAmount      = baseAmount * effectiveDiscount / 100;
        this.priceAfterDiscount  = baseAmount - discountAmount;

        double effectiveGst  = gstPercent != null ? gstPercent : 0.0;
        this.gstAmount       = priceAfterDiscount * effectiveGst / 100;
        this.totalAmount     = priceAfterDiscount + gstAmount;
    }
}