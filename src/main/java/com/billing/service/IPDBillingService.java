package com.billing.service;

import com.billing.dto.IpdBillRequestDTO;
import com.billing.model.BillingMaster;
import com.billing.model.IPDBillingDetails;

public interface IPDBillingService {
	
	IPDBillingDetails generateIpdBill(IpdBillRequestDTO request);
	
}
