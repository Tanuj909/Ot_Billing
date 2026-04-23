package com.billing.ot.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OTDoctorVisitBillingRequest {

    @NotNull(message = "operationExternalId is required")
    private Long operationExternalId;       // Billing fetch karne ke liye

    @NotNull(message = "doctorExternalId is required")
    private Long doctorExternalId;

    @NotNull(message = "doctorName is required")
    private String doctorName;

    private LocalDateTime visitTime;        // null = now()

    @NotNull(message = "fees is required")
    private Double fees;
}