package com.billing.ot.service;

import java.util.List;

import com.billing.ot.dto.OTStaffBillingRequest;
import com.billing.ot.dto.OTStaffBillingResponse;
import com.billing.ot.dto.OTStaffBillingUpdateRequest;

public interface OTStaffBillingService {

	OTStaffBillingResponse addStaffBilling(OTStaffBillingRequest request);

	List<OTStaffBillingResponse> getStaffBillingByOperationId(Long operationId);

	OTStaffBillingResponse getStaffBillingById(Long staffBillingId);

	OTStaffBillingResponse updateStaffBilling(Long staffBillingId, OTStaffBillingUpdateRequest request);

	void removeStaffBilling(Long staffBillingId);

}
