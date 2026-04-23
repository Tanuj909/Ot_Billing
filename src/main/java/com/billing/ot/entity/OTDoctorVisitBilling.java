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

@Entity
@Table(name = "ot_doctor_visit_billing")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OTDoctorVisitBilling {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ot_billing_id", nullable = false)
    private OTBillingDetails otBillingDetails;

    // Doctor info — OT system se snapshot
    private Long   doctorExternalId;
    private String doctorName;

    // Visit info
    private LocalDateTime visitTime;

    // Fees — simple, no GST/discount
    private Double fees;

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (visitTime == null) visitTime = LocalDateTime.now();
        if (fees == null) fees = 0.0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}