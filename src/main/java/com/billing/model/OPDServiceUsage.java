package com.billing.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "opd_service_usage")
@Data
public class OPDServiceUsage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long appointmentId;
    private String serviceName;
    private Double servicePrice;
    private Integer quantity;
    private Double totalPrice;

    @ManyToOne
    @JoinColumn(name = "billing_master_id")
    private BillingMaster billingMaster;
}