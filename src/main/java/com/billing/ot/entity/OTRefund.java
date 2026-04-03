package com.billing.ot.entity;

import java.time.LocalDateTime;

import com.billing.enums.PaymentMode;
import com.billing.ot.enums.OTRefundStatus;

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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ot_refund")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OTRefund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ot_billing_id", nullable = false)
    private OTBillingDetails otBillingDetails;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ot_payment_id", nullable = false)
    private OTPayment otPayment;            // Kis payment ka refund

    private Double refundAmount;
    private String reason;

    @Enumerated(EnumType.STRING)
    private PaymentMode refundMode;         // CASH, CARD, UPI, etc.

    private String referenceNumber;
    private String processedBy;

    @Enumerated(EnumType.STRING)
    private OTRefundStatus refundStatus;    // INITIATED, COMPLETED, FAILED

    private LocalDateTime refundedAt;
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
