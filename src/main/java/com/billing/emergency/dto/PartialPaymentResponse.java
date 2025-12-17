// com.billing.emergency.dto.PartialPaymentResponse.java
package com.billing.emergency.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PartialPaymentResponse {
    private Long emergencyId;
    private Long paymentHistoryId;
    private Double amountPaid;
    private Double previousDue;
    private Double newDue;
    private Double totalPaidSoFar;
    private String paymentMode;
    private LocalDateTime paymentDate;
    private String receiptNo;
    private String message = "Partial payment recorded successfully";
}