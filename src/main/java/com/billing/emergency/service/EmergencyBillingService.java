package com.billing.emergency.service;

import com.billing.emergency.dto.AddDoctorVisitRequest;
import com.billing.emergency.dto.AddDoctorVisitResponse;
import com.billing.emergency.dto.AddEmergencyItemsRequest;
import com.billing.emergency.dto.AddEmergencyItemsResponse;
import com.billing.emergency.dto.BillInActiveRequest;
import com.billing.emergency.dto.CloseEmergencyBillResponse;
import com.billing.emergency.dto.EmergencyBillResponse;
import com.billing.emergency.dto.GenerateEmergencyBillRequest;
import com.billing.emergency.dto.GetEmergencyBillResponse;
import com.billing.emergency.dto.PartialPaymentRequest;
import com.billing.emergency.dto.PartialPaymentResponse;
import com.billing.emergency.dto.PaymentHistoryResponse;
import com.billing.emergency.dto.UpdateEmergencyStayRequest;
import com.billing.emergency.dto.UpdateEmergencyStayResponse;

public interface EmergencyBillingService {
	
	//Generate Billing
	EmergencyBillResponse generateEmergencyBill(GenerateEmergencyBillRequest request);
	
	//Add Service To Billing
	AddEmergencyItemsResponse addItemsToBill(AddEmergencyItemsRequest request);

	//Update Existing Billing
	UpdateEmergencyStayResponse updateStayAndRecalculate(UpdateEmergencyStayRequest request);
	
	//Get the Emergency Billing Details
	GetEmergencyBillResponse getEmergencyBillByEmergencyId(Long emergencyId);
	
	//Partial payment
	PartialPaymentResponse recordPartialPayment(PartialPaymentRequest request);
	
	//Make Billing Inactive
	CloseEmergencyBillResponse makeBillInactive(BillInActiveRequest request);
	
	//Add doctor Fees and count and Total
	
	
	//Get Payment History
	PaymentHistoryResponse getPaymentHistory(Long emergencyId);

	AddDoctorVisitResponse addDoctorVisit(AddDoctorVisitRequest request);
}
