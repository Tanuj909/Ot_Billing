package com.billing.service;

import com.billing.dto.IpdBillRequestDTO;
import com.billing.dto.IpdPaymentRequestDTO;
import com.billing.model.IPDBillingDetails;

public interface IPDBillingService {
	
	IPDBillingDetails generateIpdBill(IpdBillRequestDTO request);
	
//	IpdBillRequestDTO getBillingDetails(Long admissionId);
	

	IPDBillingDetails getBillingDetailsByAdmissionId(Long admissionId);

	
	String processPayment(IpdPaymentRequestDTO request);
	
}
