package com.billing.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "emergency_payment_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyPaymentHistory {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long emergencyId;
    
    @Column(nullable = false)
    private Double amount;
    
    @Column(nullable = false)
    private String paymentMode; // CASH, UPI, CARD, BANK_TRANSFER
    
    @Column(nullable = false)
    private LocalDateTime paymentDate = LocalDateTime.now();
    
    private String paidBy; // Optional: "Ram Singh (Brother)", "Patient"

    private String receiptNo; // Optional: auto-generated
    
    @ManyToOne
    @JoinColumn(name = "emergency_billing_id")
    private EmergencyBillingDetails billingDetails;
    
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    
}
