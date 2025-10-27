package com.billing.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.billing.dto.OpdBillRequestDTO;
import com.billing.model.BillingMaster;
import com.billing.model.Hospital;
import com.billing.model.OPDBillingDetails;
import com.billing.model.Patient;
import com.billing.repository.BillingMasterRepository;
import com.billing.repository.HospitalRepository;
import com.billing.repository.PatientRepository;
import com.billing.service.OPDBillingService;

@Service
public class OPDBillingServiceImpl implements OPDBillingService
{
	
	@Autowired
	private BillingMasterRepository billingMasterRepository;
	
	@Autowired
	private PatientRepository patientRepository;
	
	@Autowired
	private HospitalRepository hospitalRepository;
	
	@Override
	public OPDBillingDetails generateOpdBilling(OpdBillRequestDTO request) {
		
		//Ye jo niche patient and hospital hai ye abhi Patient & Hospital type kai hai, 
		//inhea tujhea Long ka type ka krna hoga IPD mai use krne sai phle
		
		//patient
		Patient patient = patientRepository.findByExternalId(request.getPatientExternalId())
				.orElseThrow(()-> new RuntimeException("Patient not found"));
		
		//Hospital
		Hospital hospital = hospitalRepository.findByExternalId(request.getHospitalExternalId())
				.orElseThrow(()-> new RuntimeException("Hospital not found"));
		
		
		double total = request.getDoctorFee() + request.getEmergencyFee() +
				       request.getDressing() + request.getInjection() + request.getMinorProcedure();
		
		
		BillingMaster billingMaster = new BillingMaster();
		billingMaster.setHospital(hospital);
		billingMaster.setPatient(patient);
		billingMaster.setModuleType("OPD");
		billingMaster.setPaymentStatus(request.getPaymentStatus());
		billingMaster.setTotalAmount(total);
		billingMasterRepository.save(billingMaster);
		
		return null;
	}
	
}
