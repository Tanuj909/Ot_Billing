// IPDMedication.java
package com.billing.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ipd_medications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class IPDMedication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ipd_billing_id", nullable = false)
    private IPDBillingDetails ipdBillingDetails;

    private String medicineName;
    private Integer quantity;
    private Double pricePerUnit;
    private Double totalPrice;         // quantity * pricePerUnit
    private String dosage;            // Optional: e.g., "1 tab daily"
}