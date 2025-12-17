package com.billing.service;

import java.util.List;

import com.billing.dto.OpdBillRequestDTO;
import com.billing.dto.OpdBillingDeatilsResponse;
import com.billing.dto.OpdPaymentRequestDTO;
import com.billing.dto.OpdServiceUsageRequestDTO;
import com.billing.dto.OpdServiceUsageResponseDTO;
import com.billing.model.OPDBillingDetails;

public interface OPDBillingService {
	
//	OPDBillingDetails generateOpdBilling(OpdBillRequestDTO request);
	
	String processPayment(OpdPaymentRequestDTO request);
	
	OpdBillingDeatilsResponse getBillingDetailsByAppointmentId(Long appointmentId);

	String addServicesToBilling(List<OpdServiceUsageRequestDTO> requests);

//	OPDBillingDetails payDoctorFees(OpdBillRequestDTO request);

//	OPDBillingDetails createInitialBill(OpdBillRequestDTO request);

	OPDBillingDetails generateOpdBilling(OpdBillRequestDTO request);
	
	List<OpdServiceUsageResponseDTO> getAddedServicesByAppointmentId(Long appointmentId);

	
}
