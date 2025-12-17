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

    @NotNull(message = "daysAdmitted is required")
    @Positive(message = "daysAdmitted must be positive")
    private Long daysAdmitted;

    // Only these two per-day rates are updated by Emergency service
    private Double monitoringChargesPerDay;
    private Double nursingChargesPerDay;
}