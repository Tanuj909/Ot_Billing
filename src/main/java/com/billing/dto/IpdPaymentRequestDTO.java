package com.billing.dto;

import com.billing.enums.PaymentMode;
import lombok.Data;

@Data
public class IpdPaymentRequestDTO {
	private Long admissionId;
	private PaymentMode paymentMode;
//	private double amount;
//	private LocalDate payment_date;
//	private String transaction_reference;

}
