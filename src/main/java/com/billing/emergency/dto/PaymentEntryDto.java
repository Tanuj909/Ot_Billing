// Nested DTO for each payment
package com.billing.emergency.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaymentEntryDto {
    private Long paymentId;                 // From EmergencyPaymentHistory.id
    private Double amount;
    private String paymentMode;             // CASH, UPI, CARD, etc.
    private LocalDateTime paymentDate;
    private String paidBy;                  // e.g., "Patient", "Brother"
    private String receiptNo;
}