package com.billing.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.billing.dto.ChangeIsDailyRequestDTO;
import com.billing.dto.CreateIpdBillingAccountRequest;
import com.billing.dto.CreateIpdBillingAccountResponse;
import com.billing.dto.IpdBillGenerateRequestDTO;
import com.billing.dto.IpdBillRequestDTO;
import com.billing.dto.IpdBillUpdateRequestDTO;
import com.billing.dto.IpdBillingDetailsResponse;
import com.billing.dto.IpdPartialPaymentRequestDTO;
import com.billing.dto.IpdPaymentRequestDTO;
import com.billing.dto.OpdBillRequestDTO;
import com.billing.dto.OpdBillingDeatilsResponse;
import com.billing.dto.OpdPaymentRequestDTO;
import com.billing.dto.OpdServiceUsageRequestDTO;
import com.billing.dto.OpdServiceUsageResponseDTO;
import com.billing.dto.PatientAdmissionRequest;
import com.billing.dto.SpecialDiscountRequestDTO;
import com.billing.model.IPDBillingDetails;
import com.billing.model.IPDServiceUsage;
import com.billing.model.IpdPaymentHistory;
import com.billing.model.OPDBillingDetails;
import com.billing.repository.BillingMasterRepository;
import com.billing.repository.IpdPaymentHistoryRepository;
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
	  
	  @Autowired
	  private IpdPaymentHistoryRepository ipdPaymentHistoryRepository;
	  
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
    
//    @PostMapping("/ipd/create-billing-account")
//    public ResponseEntity<CreateIpdBillingAccountResponse> createIpdBillingAccount(
//            @RequestBody CreateIpdBillingAccountRequest request) {
//        
//        CreateIpdBillingAccountResponse response = ipdBillingService.createBillingAccount(request);
//        return ResponseEntity.ok(response);
//    }
    
    //Will generate the Bill for IPD
	@PostMapping("ipd/generate-bill")
	public ResponseEntity<IPDBillingDetails> generateIPDBill(@RequestBody IpdBillRequestDTO request){
		return ResponseEntity.ok(ipdBillingService.generateIpdBill(request));
	}
	

	@PostMapping("/ipd/close-bill/{admissionId}")
	public ResponseEntity<Void> closeBill(@PathVariable Long admissionId) {
	    ipdBillingService.closeBillOnDischarge(admissionId);
	    return ResponseEntity.ok().build();
	}

//	New End-Point for partial Payment(testing)
	@PostMapping("/ipd/partial-payment")
	public ResponseEntity<String> makePartialPayment(@RequestBody IpdPartialPaymentRequestDTO request) {
	    ipdBillingService.makePartialPayment(request);
	    return ResponseEntity.ok("₹" + request.getAmount() + " received successfully. Due amount updated.");
	}
	
//	Get Payment History
	@GetMapping("/ipd/payment-history/{admissionId}")
	public ResponseEntity<List<IpdPaymentHistory>> getPaymentHistory(@PathVariable Long admissionId) {
	    List<IpdPaymentHistory> history = ipdPaymentHistoryRepository
	        .findByAdmissionIdOrderByPaymentDateDesc(admissionId);
	    return ResponseEntity.ok(history);
	}
	
	@GetMapping("/ipd/status")
	public ResponseEntity<String> getPaymentStatus(@RequestParam Long admissionId){
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
	
	// Special Discount
	@PostMapping("ipd/special-discount")
	public ResponseEntity<IPDBillingDetails> specialDiscount(@RequestBody SpecialDiscountRequestDTO request){
		IPDBillingDetails updated = ipdBillingService.specialDiscounts(request);
		return ResponseEntity.ok(updated);
		}
	
//	Update Service Isdaily
	@PutMapping("/ipd/service/change-daily-status")
	public ResponseEntity<IPDServiceUsage> changeServiceDailyStatus(@RequestBody ChangeIsDailyRequestDTO request) {
	    IPDServiceUsage updated = ipdBillingService.changeServiceDailyStatus(request);
	    return ResponseEntity.ok(updated);
	}
	
//	Set Release time for the Admissio Room(Can be used for Other Purpose As well!)
	@PutMapping("/ipd/{admissionId}/release-room")
	public ResponseEntity<String> releaseRoom(@PathVariable Long admissionId) {

	    ipdBillingService.releaseCurrentRoom(admissionId);

	    return ResponseEntity.ok("Room released successfully");
	}
	
//	Pause Billing for Transfer Purpose(Can be used for Other Purpose As well!)
	@PutMapping("/ipd/pause-bill/{admissionId}")
	public ResponseEntity<Void> pauseBill(@PathVariable Long admissionId) {

	    ipdBillingService.pauseBill(admissionId);
	    return ResponseEntity.ok().build();
	}
	
//	Resume Billing for Transfer Purpose(Can be used for Other Purpose As well!)
	@PutMapping("/ipd/resume-bill/{admissionId}")
	public ResponseEntity<Void> resumeBill(@PathVariable Long admissionId) {

	    ipdBillingService.resumeBill(admissionId);
	    return ResponseEntity.ok().build();
	}
	
//	OPD Controller---------------
	
	@PostMapping("/opd/generate-bill")
	public ResponseEntity<OPDBillingDetails> generateOPDBill(@RequestBody OpdBillRequestDTO request){
		return ResponseEntity.ok(opdBillingService.generateOpdBilling(request));
	}
	
//	@PutMapping("/opd/payment")
//	public ResponseEntity<String> makeOPDPayment(@RequestBody OpdPaymentRequestDTO request){
//		String result = opdBillingService.processPayment(request);
//		return ResponseEntity.ok(result);
//	}
	
	@GetMapping("/opd/billing-detail/{appointmentId}")
	public ResponseEntity<OpdBillingDeatilsResponse> getOPDBillingDetails(@PathVariable Long appointmentId){
		OpdBillingDeatilsResponse response = opdBillingService.getBillingDetailsByAppointmentId(appointmentId);
		return ResponseEntity.ok(response);
	}
	
	@PostMapping("/opd/add-services")
	public ResponseEntity<String> addService(@RequestBody List<OpdServiceUsageRequestDTO> requests){
		return ResponseEntity.ok(opdBillingService.addServicesToBilling(requests));
	}
	
	@PostMapping("/opd/process-payment")
	public ResponseEntity<String> processFinalPayment(@RequestBody OpdPaymentRequestDTO request) {
	    String result = opdBillingService.processPayment(request);
	    return ResponseEntity.ok(result);
	}

	@GetMapping("/opd/services/{appointmentId}")
	public ResponseEntity<List<OpdServiceUsageResponseDTO>> getAddedServices(
	        @PathVariable Long appointmentId) {
	    
	    List<OpdServiceUsageResponseDTO> services = 
	        opdBillingService.getAddedServicesByAppointmentId(appointmentId);
	    
	    return ResponseEntity.ok(services);
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
