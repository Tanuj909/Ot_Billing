package com.billing.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.billing.model.BillingMaster;
import com.billing.repository.BillingMasterRepository;
import com.billing.service.PatientBillingService;

@Service
public class PatientBillingServiceImpl implements PatientBillingService{
	
	@Autowired
	private BillingMasterRepository billingMasterRepository;

	@Override
	public List<BillingMaster> getPatientBilling(Long externalId) {
		return billingMasterRepository.findByPatient_Id(externalId);
	}

}
