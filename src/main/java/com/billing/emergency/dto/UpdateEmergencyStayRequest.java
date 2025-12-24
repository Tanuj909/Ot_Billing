// com.billing.emergency.dto.UpdateEmergencyStayRequest.java
package com.billing.emergency.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEmergencyStayRequest {

    @NotNull(message = "emergencyId is required")
    private Long emergencyId;
    
    private Long totalHoursAdmitted;

    // Only these two per-day rates are updated by Emergency service
    private Double monitoringChargesPerDay;
    private Double nursingChargesPerDay;
}