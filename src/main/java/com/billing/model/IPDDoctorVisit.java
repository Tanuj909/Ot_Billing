// IPDDoctorVisit.java
package com.billing.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "ipd_doctor_visits")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class IPDDoctorVisit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ipd_billing_id", nullable = false)
    private IPDBillingDetails ipdBillingDetails;

    private LocalDate visitDate;
    private String doctorName;
    private Double consultationFee;
}