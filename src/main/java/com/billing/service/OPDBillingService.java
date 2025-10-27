package com.billing.service;

import com.billing.dto.OpdBillRequestDTO;
import com.billing.model.OPDBillingDetails;

public interface OPDBillingService {
	
	OPDBillingDetails generateOpdBilling(OpdBillRequestDTO request);
	
}
