package com.billing.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.billing.dto.IpdBillGenerateRequestDTO;
import com.billing.dto.IpdBillRequestDTO;
import com.billing.dto.IpdBillUpdateRequestDTO;
import com.billing.dto.IpdBillingDetailsResponse;
import com.billing.dto.IpdPaymentRequestDTO;
import com.billing.dto.OpdBillRequestDTO;
import com.billing.dto.OpdBillingDeatilsResponse;
import com.billing.dto.OpdPaymentRequestDTO;
import com.billing.dto.PatientAdmissionRequest;
import com.billing.model.IPDBillingDetails;
import com.billing.model.OPDBillingDetails;
import com.billing.repository.BillingMasterRepository;
import com.billing.service.BillingService;
import com.billing.service.IPDBillingService;
import com.billing.service.OPDBillingService;


@RestController
@RequestMapping("api/billing")
public class BillingController {
	
	  @Autowired
	  private IPDBillingService ipdBillingService;
	  
	  @Autowired
	  private OPDBillingService opdBillingService;
	  
	  @Autowired
	  private BillingService billingService;
	  
	  @Autowired
	  private BillingMasterRepository billingMasterRepository;
	  
//	  @Autowired
//	  private PatientBillingService patientBillingService;
//	  
//	  @Autowired
//	  private HospitalBillingService hospitalBillingService;
	  
    @PostMapping("/register-admission")
    public ResponseEntity<String> registerPatientAdmission(
            @RequestBody PatientAdmissionRequest request) {
        billingService.registerPatientAdmission(request.getPatient(), request.getHospital(), request.getModuleType());
        return ResponseEntity.ok("Patient admission registered successfully");
    }
    
    // IPD Controller
    
    //Will generate the Bill for IPD
	@PostMapping("ipd/generate-bill")
	public ResponseEntity<IPDBillingDetails> generateIPDBill(@RequestBody IpdBillRequestDTO request){
		return ResponseEntity.ok(ipdBillingService.generateIpdBill(request));
	}
	

	@PostMapping("/ipd/payment")
	public ResponseEntity<String> makeIPDPayment(@RequestBody IpdPaymentRequestDTO request){	
	String result = ipdBillingService.processPayment(request); 
	return ResponseEntity.ok(result);
	}
	
	@GetMapping("/ipd/status")
	public ResponseEntity<String> getPaymentStatus(Long admissionId){
		String status = billingMasterRepository.findByAdmissionId(admissionId)
	            .map(b -> b.getPaymentStatus().name())
	            .orElse("NOT_FOUND");
		return ResponseEntity.ok(status);
	}
	
	@GetMapping("/details/{admissionId}")
	public ResponseEntity<IpdBillingDetailsResponse> getIPDBillingDetails(@PathVariable Long admissionId) {
		IpdBillingDetailsResponse response = ipdBillingService.getBillingDetailsByAdmissionId(admissionId);
	    return ResponseEntity.ok(response);
	}
	
	// ADD THIS ENDPOINT
	@PutMapping("/ipd/update-bill")
	public ResponseEntity<IPDBillingDetails> updateIPDBill(@RequestBody IpdBillUpdateRequestDTO request) {
	    IPDBillingDetails updated = ipdBillingService.updateIpdBill(request);
	    return ResponseEntity.ok(updated);
	}
	
//	OPD Controller---------------
	
	@PostMapping("/opd/generate-bill")
	public ResponseEntity<OPDBillingDetails> generateOPDBill(@RequestBody OpdBillRequestDTO request){
		return ResponseEntity.ok(opdBillingService.generateOpdBilling(request));
	}
	
	@PutMapping("/opd/payment")
	public ResponseEntity<String> makeOPDPayment(@RequestBody OpdPaymentRequestDTO request){
		String result = opdBillingService.processPayment(request);
		return ResponseEntity.ok(result);
	}
	
	@GetMapping("/opd/billing-detail/{appointmentId}")
	public ResponseEntity<OpdBillingDeatilsResponse> getOPDBillingDetails(@PathVariable Long appointmentId){
		OpdBillingDeatilsResponse response = opdBillingService.getBillingDetailsByAppoitmnetId(appointmentId);
		return ResponseEntity.ok(response);
	}
	

	
	//Get the Billing of the patient(Will give list if there more than one)
//	@GetMapping("/patient/{patientId}")
//	public List<BillingMaster> getPatientBilling(@PathVariable Long patientId) {
//		return patientBillingService.getPatientBilling(patientId);
//	}
//	
//	//Get the List of Bills associated with the given Hospital ID!
//	@GetMapping("/hospital/{hospitalId}")
//	public List<BillingMaster> getHospitalBilling(@PathVariable Long hospitalId) {
//		return hospitalBillingService.findHispitalById(hospitalId);
//	}
	
	//Get the Total amount for Bills(All bills total)
//	@GetMapping("/hospital/{hospitalId}/total")
//	public Double getHospitalTotalBilling(@PathVariable Long hospitalId) {
//	    return hospitalBillingService.getTotalBillingByHospitalId(hospitalId);
//	}
//	
//	//Get the Billing with PENDING & PAID status
//	@GetMapping("/payment")
//	public List<BillingMaster> getBillingByPaymentStatus(@RequestParam Long hospitalId, @RequestParam PaymentStatus status){
//		return hospitalBillingService.findBillingByPayment_Status(hospitalId, status);
//	}
	
//	//Get the Billing with Module Type
//	@GetMapping("/moduleType")
//	public List<BillingMaster> getBillingByModule(
//			@RequestParam Long hospitalId, @RequestParam String moduleType){
//		
//		return hospitalBillingService.findBillingByModuleType(hospitalId, moduleType);
//	}
	
//	@GetMapping("hospital/{hospitalId}/module/{moduleType}")
//	public List<BillingMaster> getBillingByModule(
//			  @PathVariable Long hospitalId,
//		       @PathVariable String moduleType){
//		
//		return hospitalBillingService.findBillingByModuleType(hospitalId, moduleType);
//	}
	
	
	
}
