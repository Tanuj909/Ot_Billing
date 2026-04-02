package com.billing.ot.dto;

import java.time.LocalDateTime;

import com.billing.enums.PaymentMode;
import com.billing.enums.PaymentStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OTBillingMasterResponse {

	    private Long id;
	    private Long hospitalExternalId;
	    private Long patientExternalId;
	    private Long otOperationId;
	    private String moduleType;
	    private Double totalAmount;
	    private PaymentStatus paymentStatus;
	    private PaymentMode paymentMode;
	    private String advancePaymentMode;
	    private LocalDateTime billingDate;
	    private LocalDateTime updatedAt;

}
