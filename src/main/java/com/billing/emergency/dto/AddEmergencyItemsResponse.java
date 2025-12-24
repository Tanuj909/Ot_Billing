// Response DTO
package com.billing.emergency.dto;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

@Data
public class AddEmergencyItemsResponse {
    private Long emergencyId;
    private int itemsAdded;
    private Double newTotalAdded;
    private Double updatedBillTotal;
    private Double updatedDue;
//    @Enumerated(EnumType.STRING)
//    private HourlyCharges hourlyCharges;
    private String message = "Items added successfully to emergency bill";
}