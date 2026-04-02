package com.billing.ot.service;

import com.billing.ot.dto.OTBillingDetailsRequest;
import com.billing.ot.dto.OTBillingDetailsResponse;

public interface OTBillingDetailsService {

	OTBillingDetailsResponse createOTBillingDetails(OTBillingDetailsRequest request);

	OTBillingDetailsResponse getByOperationId(Long operationId);

	OTBillingDetailsResponse getById(Long id);

	OTBillingDetailsResponse recalculateTotals(Long operationId);

	OTBillingDetailsResponse closeBilling(Long operationId);

}
