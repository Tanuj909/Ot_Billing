package com.billing.ot.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OTDoctorVisitBillingUpdateRequest {

    // Sirf fees aur visitTime update ho sakti hai
    private Double fees;
    private LocalDateTime visitTime;
}