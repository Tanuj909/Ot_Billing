// package com.billing.model;

package com.billing.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "emergency_doctor_visit")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyDoctorVisit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emergency_billing_id", nullable = false)
    private EmergencyBillingDetails emergencyBillingDetails;

    // Doctor details (external IDs if you have Doctor MS)
    private Long doctorExternalId;       // Optional: link to Doctor microservice
    private String doctorName;           // Required: display name

    private Double feesPerVisit;         // Fees charged per visit

    private Integer visitCount = 1;      // Default 1, can be increased later

    private Double totalFees;            // = feesPerVisit × visitCount (calculated)

    private LocalDateTime firstVisitDate = LocalDateTime.now();

    private LocalDateTime lastVisitDate;

    private String remarks;              // Optional: e.g., "Consultation", "Follow-up"

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;
}