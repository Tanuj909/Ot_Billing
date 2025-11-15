package com.billing.dto;

import com.billing.enums.PaymentMode;

import lombok.Data;

@Data
public class OpdPaymentRequestDTO {
	
	private Long appointmentId;
	private PaymentMode paymentMode;

}
