// Nested DTO for items
package com.billing.emergency.dto;

import com.billing.enums.EmergencyItemsCategory;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EmergencyBillingItemDto {
    private String serviceName;
    private String category;
    private Double price;
    private Integer quantity;
    private Double totalAmount;
    private Double gstPercentage;
    private Double gstAmount;
    private LocalDateTime serviceAddDate;
    @Enumerated(EnumType.STRING)
    private IsHourly isHourly;
}