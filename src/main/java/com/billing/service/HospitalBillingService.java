package com.billing.service;

import java.util.List;

import com.billing.enums.PaymentStatus;
import com.billing.model.BillingMaster;


public interface HospitalBillingService {
	
	List<BillingMaster> findHispitalById(Long hospitalId);
	
	Double getTotalBillingByHospitalId(Long hospitalId);
	
	//Method to get the Bills with PENDING status
	List<BillingMaster> findBillingByPayment_Status(Long hospitalId, PaymentStatus paymentStatus);
	
	//Method to get the Bills by Module Type
	List<BillingMaster> findBillingByModuleType(Long hospitalId, String moduleType);

}
