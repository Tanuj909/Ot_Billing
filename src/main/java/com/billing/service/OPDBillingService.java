package com.billing.service;

import com.billing.dto.OpdBillRequestDTO;
import com.billing.dto.OpdBillingDeatilsResponse;
import com.billing.dto.OpdPaymentRequestDTO;
import com.billing.model.OPDBillingDetails;

public interface OPDBillingService {
	
	OPDBillingDetails generateOpdBilling(OpdBillRequestDTO request);
	
	String processPayment(OpdPaymentRequestDTO request);
	
	OpdBillingDeatilsResponse getBillingDetailsByAppoitmnetId(Long appointmentId);
	
}
