package com.billing.ot.service;

import java.util.List;

import com.billing.ot.dto.BillingMasterRequest;
import com.billing.ot.dto.BillingMasterResponse;
import com.billing.ot.dto.BillingMasterUpdateRequest;
import com.billing.ot.dto.OTBillingMasterRequest;

public interface BillingMasterService {

	BillingMasterResponse createBilling(OTBillingMasterRequest request);

	BillingMasterResponse getBillingById(Long billingId);

	BillingMasterResponse getBillingByOperationId(Long operationId);

	List<BillingMasterResponse> getBillingByPatientId(Long patientId);

	List<BillingMasterResponse> getBillingByModuleType(String moduleType);

	BillingMasterResponse updateBilling(Long billingId, BillingMasterUpdateRequest request);

	void cancelBilling(Long billingId);



}
