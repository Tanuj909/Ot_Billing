package com.billing.ot.entity;

import java.time.LocalDateTime;

import com.billing.ot.enums.OTItemType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

@Entity
@Table(name = "ot_item_billing")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OTItemBilling {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ot_billing_id", nullable = false)
    private OTBillingDetails otBillingDetails;

    private Long itemExternalId;            // OT system ka item id
    
    @Enumerated(EnumType.STRING)
    private OTItemType itemType;            // IV_FLUID, ANESTHESIA_DRUG, CONSUMABLE, IMPLANT, EQUIPMENT
    
    private String itemName;
    private String itemCode;                // OTItemCatalog se
    private String hsnCode;

    private Integer quantity;
    private Double unitPrice;

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
        if (unitPrice == null || quantity == null) return;

        double baseAmount = unitPrice * quantity;

        double effectiveDiscount = discountPercent != null ? discountPercent : 0.0;
        this.discountAmount = baseAmount * effectiveDiscount / 100;
        this.priceAfterDiscount = baseAmount - discountAmount;

        double effectiveGst = gstPercent != null ? gstPercent : 0.0;
        this.gstAmount = priceAfterDiscount * effectiveGst / 100;

        this.totalAmount = priceAfterDiscount + gstAmount;
    }
}