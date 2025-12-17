package com.billing.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "opd_billing_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OPDBillingDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double doctorFee = 0.0;
    private Double serviceCharges = 0.0;     // Total of all added services

    private Double emergencyFee = 0.0;
    private Double dressing = 0.0;
    private Double injection = 0.0;
    private Double minorProcedure = 0.0;

    private Double totalFees;     // doctorFee + serviceCharges (gross)
    private Double payableAmount; // What patient still owes (serviceCharges - doctorFee)

    private LocalDateTime visitDate;
    private Long appointmentId;
    private Long doctorId;

    @OneToOne
    @JoinColumn(name = "billing_id", nullable = false)
    private BillingMaster billingMaster;
}