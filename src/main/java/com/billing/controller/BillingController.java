package com.billing.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.billing.dto.IpdBillRequestDTO;
import com.billing.dto.PatientAdmissionRequest;
import com.billing.model.BillingMaster;
import com.billing.model.IPDBillingDetails;
import com.billing.service.BillingService;
import com.billing.service.IPDBillingService;

@RestController
@RequestMapping("api/billing")
public class BillingController {
	
	@Autowired
	private IPDBillingService ipdBillingService;
	
	  @Autowired
	  private BillingService billingService;

	
    @PostMapping("/register-admission")
    public ResponseEntity<String> registerPatientAdmission(
            @RequestBody PatientAdmissionRequest request) {
        billingService.registerPatientAdmission(request.getPatient(), request.getHospital(), request.getModuleType());
        return ResponseEntity.ok("Patient admission registered successfully");
    }

    
	@PostMapping("ipd/generate-bill")
	public ResponseEntity<IPDBillingDetails> generateIPDBill(@RequestBody IpdBillRequestDTO request)
	{
		return ResponseEntity.ok(ipdBillingService.generateIpdBill(request));
	}
}
