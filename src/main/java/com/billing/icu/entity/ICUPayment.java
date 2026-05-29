package com.billing.icu.entity;

import com.billing.enums.PaymentMode;
import com.billing.ot.enums.OTPaymentStatus;
import com.billing.ot.enums.OTPaymentType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * ICU ke liye payment record — OTPayment ki tarah.
 */
@Entity
@Table(name = "icu_payment",
        indexes = {
                @Index(name = "idx_icu_payment_billing", columnList = "icu_billing_id"),
                @Index(name = "idx_icu_payment_patient", columnList = "patientExternalId")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ICUPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "icu_billing_id", nullable = false)
    private ICUBillingDetails icuBillingDetails;

    private Long patientExternalId;

    @Enumerated(EnumType.STRING)
    private OTPaymentType paymentType;      // ADVANCE / PARTIAL / FULL

    @Enumerated(EnumType.STRING)
    private PaymentMode paymentMode;        // CASH / CARD / UPI etc.

    private Double amount;
    private String referenceNumber;
    private String receivedBy;

    @Enumerated(EnumType.STRING)
    private OTPaymentStatus status;         // SUCCESS / FAILED / REFUNDED

    private String notes;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        paidAt    = LocalDateTime.now();
    }
}