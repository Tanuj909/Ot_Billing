package com.billing.ot.dto;

import lombok.Data;

@Data
public class OTBillingDetailsRequest {
    private Long billingMasterId;
    private String operationReference;
}