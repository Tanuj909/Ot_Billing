package com.billing.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.billing.enums.PaymentStatus;
import com.billing.model.BillingMaster;
import com.billing.repository.BillingMasterRepository;
import com.billing.service.HospitalBillingService;

@Service
public class HospitalBillingServiceImpl implements HospitalBillingService{
	
	@Autowired
	private BillingMasterRepository billingMasterRepository;

	@Override
	public List<BillingMaster> findHispitalById(Long hospitalId) {
		return billingMasterRepository.findByHospital_Id(hospitalId);
	}

	@Override
	public Double getTotalBillingByHospitalId(Long hospitalId) {
		double totalAmmount = billingMasterRepository.getTotalBillingByHospitalId(hospitalId);
		return totalAmmount;
	}

	@Override
	public List<BillingMaster> findBillingByPayment_Status(Long hospitalId , PaymentStatus paymentStatus) {
		List<BillingMaster> status = billingMasterRepository.findBillingByHospitalIdAndPaymentStatus(hospitalId,paymentStatus);
		return status;
	}

//	@Override
//	public List<BillingMaster> findBillingByModuleType(Long hospitalId, String moduleType) {
//		return billingMasterRepository.findBillingByHospitalIdAndModuleType(hospitalId, moduleType); 
//	}

}
