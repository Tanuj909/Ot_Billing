package com.billing.emergency.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddDoctorVisitRequest {

    @NotNull
    private Long emergencyId;

    private Long doctorExternalId;

    @NotBlank
    private String doctorName;

    @NotNull
    @Positive
    private Double feesPerVisit;

    @Min(1)
    private Integer visitCount = 1;  // How many visits (default 1)

    private String remarks;
}