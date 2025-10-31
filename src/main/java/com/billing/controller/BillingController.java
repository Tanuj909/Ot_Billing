package com.billing.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.billing.dto.IpdBillRequestDTO;
import com.billing.dto.IpdBillingDetailsResponse;
import com.billing.dto.IpdPaymentRequestDTO;
import com.billing.dto.OpdBillRequestDTO;
import com.billing.dto.PatientAdmissionRequest;
import com.billing.enums.PaymentStatus;
import com.billing.model.BillingMaster;
import com.billing.model.IPDBillingDetails;
import com.billing.model.OPDBillingDetails;
import com.billing.repository.BillingMasterRepository;
import com.billing.service.BillingService;
import com.billing.service.HospitalBillingService;
import com.billing.service.IPDBillingService;
import com.billing.service.OPDBillingService;
import com.billing.service.PatientBillingService;

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
    
    //Will generate the Bill for IPD
	@PostMapping("ipd/generate-bill")
	public ResponseEntity<IPDBillingDetails> generateIPDBill(@RequestBody IpdBillRequestDTO request){
		return ResponseEntity.ok(ipdBillingService.generateIpdBill(request));
	}
	
	@PostMapping("/opd/generate-bill")
	public ResponseEntity<OPDBillingDetails> generateOPDBill(@RequestBody OpdBillRequestDTO request){
		return ResponseEntity.ok(opdBillingService.generateOpdBilling(request));
	}
	
	@PostMapping("/ipd/payment")
	public ResponseEntity<String> makePayment(@RequestBody IpdPaymentRequestDTO request){	
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
	public ResponseEntity<IpdBillingDetailsResponse> getBillingDetails(@PathVariable Long admissionId) {
	    IPDBillingDetails details = ipdBillingService.getBillingDetailsByAdmissionId(admissionId);

	    IpdBillingDetailsResponse response = new IpdBillingDetailsResponse();
	    response.setId(details.getId());
	    response.setAdmissionId(details.getAdmissionId());
	    response.setRoomCharges(details.getRoomCharges());
	    response.setMedicationCharges(details.getMedicationCharges());
	    response.setDoctorFees(details.getDoctorFees());
	    response.setNursingCharges(details.getNursingCharges());
	    response.setDiagnosticCharges(details.getDiagnosticCharges());
	    response.setProcedureCharges(details.getProcedureCharges());
	    response.setFoodCharges(details.getFoodCharges());
	    response.setMiscellaneousCharges(details.getMiscellaneousCharges());
	    response.setDaysAdmitted(details.getDaysAdmitted());
	    response.setTotal(details.getTotal());

	    IpdBillingDetailsResponse.BillingMasterDTO bm = new IpdBillingDetailsResponse.BillingMasterDTO();
	    bm.setId(details.getBillingMaster().getId());
	    bm.setHospitaExternallId(details.getBillingMaster().getHospitaExternallId());
	    bm.setPatientExternalId(details.getBillingMaster().getPatientExternalId());
	    bm.setAdmissionId(details.getBillingMaster().getAdmissionId());
	    bm.setModuleType(details.getBillingMaster().getModuleType());
	    bm.setTotalAmount(details.getBillingMaster().getTotalAmount());
	    bm.setPaymentStatus(details.getBillingMaster().getPaymentStatus().name());
	    bm.setPaymentMode(details.getBillingMaster().getPaymentMode() != null ? details.getBillingMaster().getPaymentMode().name() : null);
	    bm.setBillingDate(details.getBillingMaster().getBillingDate());

	    response.setBillingMaster(bm);

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
