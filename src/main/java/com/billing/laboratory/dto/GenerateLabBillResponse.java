package com.billing.laboratory.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GenerateLabBillResponse {

    private Long billingId;
    private Long labBillingId;
    private Double totalAmount;
    private Double dueAmount;
    private String billingStatus;
}
