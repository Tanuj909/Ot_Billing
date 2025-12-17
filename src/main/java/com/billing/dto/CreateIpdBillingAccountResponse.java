// src/main/java/com/billing/dto/CreateIpdBillingAccountResponse.java
package com.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateIpdBillingAccountResponse {
    private Long billingId;
    private Long admissionId;
    private Double advancePaid;
    private Double totalCharges;
    private Double dueAmount;
    private String billingStatus; // ACTIVE
    private String message;
}