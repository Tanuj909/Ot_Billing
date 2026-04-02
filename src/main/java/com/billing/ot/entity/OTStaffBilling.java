package com.billing.ot.entity;

import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//2. OTStaffBilling
@Entity
@Table(name = "ot_staff_billing")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OTStaffBilling {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ot_billing_id", nullable = false)
    private OTBillingDetails otBillingDetails;

    private Long staffExternalId;           // OT system ka userId
    private String staffName;
    private String staffRole;               // SURGEON, ANESTHESIOLOGIST, SCRUB_NURSE etc.

    private Double fees;
    private Double discountPercent;
    private Double discountAmount;
    private Double priceAfterDiscount;
    private Double gstPercent;
    private Double gstAmount;
    private Double totalAmount;

    private LocalDateTime serviceAddedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        serviceAddedAt = LocalDateTime.now();
        calculateAmounts();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calculateAmounts();
    }

    public void calculateAmounts() {
        if (fees == null) return;

        double effectiveDiscount = discountPercent != null ? discountPercent : 0.0;
        this.discountAmount = fees * effectiveDiscount / 100;
        this.priceAfterDiscount = fees - discountAmount;

        double effectiveGst = gstPercent != null ? gstPercent : 0.0;
        this.gstAmount = priceAfterDiscount * effectiveGst / 100;

        this.totalAmount = priceAfterDiscount + gstAmount;
    }
}