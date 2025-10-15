package com.billing.service;

import com.billing.dto.HospitalDTO;
import com.billing.dto.PatientDTO;

public interface BillingService {
	
	void registerPatientAdmission(PatientDTO patient, HospitalDTO hospital);
}
