package com.billing.laboratory.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentHistoryResponse {
	
    private Long paymentId;
    private Double amount;
    private String paymentMode;
    private String referenceNumber;
    private LocalDateTime paidAt;

}
