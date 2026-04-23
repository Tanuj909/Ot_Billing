package com.billing.ot.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class OTDoctorVisitBillingResponse {

    private Long   id;
    private Long   otBillingDetailsId;
    private Long   operationExternalId;     // convenience field

    private Long   doctorExternalId;
    private String doctorName;

    private LocalDateTime visitTime;
    private Double fees;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}