// com.billing.emergency.dto.CloseEmergencyBillResponse.java
package com.billing.emergency.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CloseEmergencyBillResponse {
    private Long emergencyId;
    private String billingStatus = "CLOSED";
    private Double finalBillAmount;
    private Double totalPaid;
    private Double dueAmount;           // Should be 0.0
    private Double excessPaid;          // If totalPaid > finalBillAmount
    private LocalDateTime closedAt;
    private String message = "Emergency bill closed successfully on discharge";
}