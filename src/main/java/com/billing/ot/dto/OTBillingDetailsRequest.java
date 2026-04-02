package com.billing.ot.dto;

import lombok.Data;

@Data
public class OTBillingDetailsRequest {
    private Long billingMasterId;
//    private Long operationExternalId;
    private String operationReference;
//    private Long hospitalExternalId;
//    private Long patientExternalId;
}