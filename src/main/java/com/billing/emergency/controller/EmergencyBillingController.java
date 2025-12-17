package com.billing.emergency.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
import com.billing.emergency.service.EmergencyBillingService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/emergency-billing")
public class EmergencyBillingController {
	
	@Autowired
	private EmergencyBillingService emergencyBillingService;
	
//-------------------------------------------Generate Billing-------------------------------------------------------//
	@PostMapping("/generate")
	public ResponseEntity<EmergencyBillResponse> generateBill(@Valid @RequestBody GenerateEmergencyBillRequest request){
		EmergencyBillResponse response = emergencyBillingService.generateEmergencyBill(request);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

//-------------------------------------------Update Billing-------------------------------------------------------//
	@PutMapping("/update-stay")
	public ResponseEntity<UpdateEmergencyStayResponse> updateStay(
	        @Valid @RequestBody UpdateEmergencyStayRequest request) {
	    UpdateEmergencyStayResponse response = emergencyBillingService.updateStayAndRecalculate(request);
	    return ResponseEntity.ok(response);
	}
	
	
//-------------------------------------------Item Billing-------------------------------------------------------//
	@PostMapping("/add-items")
	public ResponseEntity<AddEmergencyItemsResponse> addItems(
	        @Valid @RequestBody AddEmergencyItemsRequest request) {
	    AddEmergencyItemsResponse response = emergencyBillingService.addItemsToBill(request);
	    return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	
//-------------------------------------------Get Billing-------------------------------------------------------//
	@GetMapping("/get/{emergencyId}")
	public ResponseEntity<GetEmergencyBillResponse> getEmergencyBill(
	        @PathVariable Long emergencyId) {
	    GetEmergencyBillResponse response = emergencyBillingService.getEmergencyBillByEmergencyId(emergencyId);
	    return ResponseEntity.ok(response);
	}
	

//-------------------------------------------Partial Payment-------------------------------------------------------//
	@PostMapping("/partial-payment")
	public ResponseEntity<PartialPaymentResponse> recordPartialPayment(
	        @Valid @RequestBody PartialPaymentRequest request) {

	    PartialPaymentResponse response = emergencyBillingService.recordPartialPayment(request);
	    return new ResponseEntity<>(response, HttpStatus.CREATED);
	}
	
//-------------------------------------------Partial Payment-------------------------------------------------------//
	@PutMapping("/close-on-discharge")
	public ResponseEntity<CloseEmergencyBillResponse> makeBillInactive(@RequestBody BillInActiveRequest request) {
		CloseEmergencyBillResponse response =  emergencyBillingService.makeBillInactive(request);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
//-------------------------------------------Payment History-------------------------------------------------------//
	@GetMapping("/payment-history/{emergencyId}")
	public ResponseEntity<PaymentHistoryResponse> getPaymentHistory(
	        @PathVariable Long emergencyId) {

	    PaymentHistoryResponse response = emergencyBillingService.getPaymentHistory(emergencyId);
	    return ResponseEntity.ok(response);
	}
	
//-------------------------------------------Add Doctor visit-------------------------------------------------------//
	@PostMapping("/add-doctor-visit")
	public ResponseEntity<AddDoctorVisitResponse> addDoctorVisit(
	        @Valid @RequestBody AddDoctorVisitRequest request) {
	    return new ResponseEntity<>(emergencyBillingService.addDoctorVisit(request), HttpStatus.CREATED);
	}
}
