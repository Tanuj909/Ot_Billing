// com.billing.emergency.dto.PartialPaymentRequest.java
package com.billing.emergency.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartialPaymentRequest {

    @NotNull(message = "emergencyId is required")
    private Long emergencyId;

    @NotNull(message = "amount is required")
    @Positive(message = "amount must be positive")
    private Double amount;

    @NotBlank(message = "paymentMode is required")
    private String paymentMode; // CASH, UPI, CARD, BANK_TRANSFER

    private String paidBy;      // e.g., "Ram Singh (Brother)", "Patient Self"
    private String receiptNo;   // Optional, can be auto-generated if blank
    private LocalDateTime paymentDate; // Optional, default now()
}