// IPDServiceUsage.java
package com.billing.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ipd_service_usage")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class IPDServiceUsage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ipd_billing_id", nullable = false)
    @JsonIgnore
    private IPDBillingDetails ipdBillingDetails;

    private String serviceName;        // e.g., "ECG", "X-Ray", "Physiotherapy"
    private Double price;
    private Integer quantity = 1;
    private Double totalAmount;        // price * quantity
    private LocalDateTime serviceAddDate = LocalDateTime.now();
 // Add these fields
    private Double gstPercentage = 0.0;
    private Double gstAmount = 0.0;  // totalAmount * gstPercentage / 100
    
    
}