package com.billing.ot.entity;

import java.time.LocalDateTime;

import com.billing.enums.PaymentMode;
import com.billing.ot.enums.OTPaymentStatus;
import com.billing.ot.enums.OTPaymentType;

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

//OTPayment
@Entity
@Table(name = "ot_payment")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OTPayment {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;

 @ManyToOne(fetch = FetchType.LAZY)
 @JoinColumn(name = "ot_billing_id", nullable = false)
 private OTBillingDetails otBillingDetails;

 private Long patientExternalId;

 @Enumerated(EnumType.STRING)
 private OTPaymentType paymentType;      // ADVANCE, PARTIAL, FULL

 @Enumerated(EnumType.STRING)
 private PaymentMode paymentMode;        // CASH, CARD, UPI, etc.

 private Double amount;
 private String referenceNumber;         // Transaction ID
 private String receivedBy;

 @Enumerated(EnumType.STRING)
 private OTPaymentStatus status;         // SUCCESS, FAILED, REFUNDED

 private String notes;
 private LocalDateTime paidAt;
 private LocalDateTime createdAt;

 @PrePersist
 protected void onCreate() {
     createdAt = LocalDateTime.now();
     paidAt = LocalDateTime.now();
 }
}

