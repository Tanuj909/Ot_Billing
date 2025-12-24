package com.billing.emergency.dto;

import com.billing.enums.EmergencyItemsCategory;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyItemDto {

    @NotBlank(message = "Service name is required")
    private String serviceName;

    private String category; // e.g., MEDICINE, LAB_TEST, PROCEDURE

    @NotNull(message = "Price is required")
    @PositiveOrZero
    private Double price;

    @PositiveOrZero
    private Integer quantity = 1;

    @PositiveOrZero
    private Double gstPercentage = 0.0; // Optional GST % for this item
    
    @Enumerated(EnumType.STRING)
    private IsHourly isHourly;
}