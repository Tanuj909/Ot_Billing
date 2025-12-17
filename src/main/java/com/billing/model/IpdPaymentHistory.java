// src/main/java/com/billing/model/IpdPaymentHistory.java
package com.billing.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ipd_payment_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IpdPaymentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long admissionId;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private String paymentMode; // CASH, UPI, CARD, BANK_TRANSFER

    @Column(nullable = false)
    private LocalDateTime paymentDate = LocalDateTime.now();

    private String paidBy; // Optional: "Ram Singh (Brother)", "Patient"

    private String receiptNo; // Optional: auto-generated

    @ManyToOne
    @JoinColumn(name = "billing_id")
    private IPDBillingDetails billingDetails;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}