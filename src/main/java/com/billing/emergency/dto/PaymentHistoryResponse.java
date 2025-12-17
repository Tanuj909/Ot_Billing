// com.billing.emergency.dto.PaymentHistoryResponse.java
package com.billing.emergency.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class PaymentHistoryResponse {

    private Long emergencyId;
    private Double initialAdvancePaid = 0.0;
    private LocalDateTime advancePaidDate;          // Usually billing creation date
    private String advancePaymentMode;              // From BillingMaster if available

    private List<PaymentEntryDto> partialPayments = new ArrayList<>();

    private Double totalPaid;
    private String message = "Payment history retrieved successfully";
}