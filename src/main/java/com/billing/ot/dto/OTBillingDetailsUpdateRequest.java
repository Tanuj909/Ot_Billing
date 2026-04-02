package com.billing.ot.dto;

import lombok.Data;

@Data
public class OTBillingDetailsUpdateRequest {
    private Double advancePaid;
    private String billingStatus;
}