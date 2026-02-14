package com.billing.laboratory.entity;

import java.time.LocalDateTime;

import com.billing.enums.PaymentMode;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lab_payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long labOrderId;
    private Long billingId;
    
    private Long patientExternalId; // ✅ ADD THIS

    private Double amount;

    @Enumerated(EnumType.STRING)
    private PaymentMode paymentMode; // CASH / CARD / UPI / ONLINE

    private String referenceNumber;  // txn id, UTR, etc.

    private LocalDateTime paidAt = LocalDateTime.now();
}
