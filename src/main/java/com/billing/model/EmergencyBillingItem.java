package com.billing.model;

import java.math.BigDecimal;
import java.security.Provider.Service;
import java.time.LocalDateTime;

import com.billing.enums.EmergencyItemsCategory;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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
@Table(name = "emergency_billing_items")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class EmergencyBillingItem  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emergency_billing_id", nullable = false)
    @JsonIgnore
    private EmergencyBillingDetails emergencyBillingDetails;
    
    @Enumerated(EnumType.STRING)
    private EmergencyItemsCategory category;
    
    private String serviceName;
    private Double price;
    private Integer quantity = 1;
    private Double totalAmount; 
    private LocalDateTime serviceAddDate = LocalDateTime.now();
    private Double gstPercentage;
    private BigDecimal gstAmount;
    
}
