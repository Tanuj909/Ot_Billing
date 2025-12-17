// src/main/java/com/billing/dto/IpdPartialPaymentRequestDTO.java
package com.billing.dto;

import lombok.Data;

@Data
public class IpdPartialPaymentRequestDTO {
    private Long admissionId;
    private Double amount;       // Amount paid now (e.g., 5000)
    private String paymentMode;  // CASH, UPI, CARD, BANK_TRANSFER
}