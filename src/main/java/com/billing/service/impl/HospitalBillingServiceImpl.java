package com.billing.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.billing.enums.PaymentStatus;
import com.billing.model.BillingMaster;
import com.billing.model.Hospital;
import com.billing.repository.BillingMasterRepository;
import com.billing.repository.HospitalRepository;
import com.billing.service.HospitalBillingService;

@Service
public class HospitalBillingServiceImpl implements HospitalBillingService{
	
	@Autowired
	private BillingMasterRepository billingMasterRepository;
	
	@Autowired
	private HospitalRepository hospitalRepository;
	
	@Override
	public List<BillingMaster> findHispitalById(Long hospitalId) {
		return billingMasterRepository.findByHospital_Id(hospitalId);
	}
	
	@Override
	public Double getTotalBillingByHospitalId(Long hospitalId) {
		Hospital hospital = hospitalRepository.findById(hospitalId)
				.orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hospital Not Found"));
		
		double totalAmmount = billingMasterRepository.getTotalBillingByHospitalId(hospitalId);
		return totalAmmount;
	}
	
	@Override
	public List<BillingMaster> findBillingByPayment_Status(Long hospitalId , PaymentStatus paymentStatus) {
		Hospital hospital = hospitalRepository.findById(hospitalId)
				.orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hospital Not Found"));
		
		List<BillingMaster> status = billingMasterRepository.findBillingByHospitalIdAndPaymentStatus(hospitalId,paymentStatus);
		return status;
	}
	
	@Override
	public List<BillingMaster> findBillingByModuleType(Long hospitalId, String moduleType){
		Hospital hospital = hospitalRepository.findById(hospitalId)
				.orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hospital Not Found"));
		
		List<BillingMaster> module = billingMasterRepository.findBillingByHospital_IdAndModuleType(hospitalId, moduleType);
		return module;
	}

//	@Override
//	public List<BillingMaster> findBillingByModuleType(Long hospitalId, String moduleType) {
//		return billingMasterRepository.findBillingByHospitalIdAndModuleType(hospitalId, moduleType); 
//	}

}
