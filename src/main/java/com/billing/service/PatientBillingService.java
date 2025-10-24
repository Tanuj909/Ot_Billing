package com.billing.service;

import java.util.List;

import com.billing.model.BillingMaster;

public interface PatientBillingService 
{
	
	List<BillingMaster> getPatientBilling(Long externalId);
	
}
