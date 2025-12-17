// src/main/java/com/billing/dto/CreateIpdBillingAccountRequest.java
package com.billing.dto;

import lombok.Data;

@Data
public class CreateIpdBillingAccountRequest {
    private Long admissionId;
    private Long patientExternalId;
    private Long hospitalExternalId;
    private Double advanceAmount; // Optional, can be 0 or null
    private String paymentMode;  // CASH, UPI, CARD, etc.
}