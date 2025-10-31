
package com.billing.service.impl;

import java.time.temporal.ChronoUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.billing.dto.IpdBillRequestDTO;
import com.billing.dto.IpdPaymentRequestDTO;
import com.billing.enums.PaymentStatus;
import com.billing.model.BillingMaster;
import com.billing.model.Hospital;
import com.billing.model.IPDBillingDetails;
import com.billing.model.Patient;
import com.billing.repository.BillingMasterRepository;
import com.billing.repository.HospitalRepository;
import com.billing.repository.IPDBillingRepository;
import com.billing.repository.PatientRepository;
import com.billing.service.IPDBillingService;

@Service
public class IPDBillingServiceImpl implements IPDBillingService{
	
	@Autowired
	private  IPDBillingRepository ipdBillingRepository;
	
	@Autowired
	private  BillingMasterRepository billingMasterRepository;
	
	@Autowired
	private  HospitalRepository hospitalRepository;
	
	@Autowired
	private  PatientRepository patientRepository;
	
	@Autowired
	private RestTemplate restTemplate;

	@Override
	public IPDBillingDetails generateIpdBill(IpdBillRequestDTO request) {
		
		//Ye jo niche patient and hospital hai ye abhi Patient & Hospital type kai hai, 
		//inhea tujhea Long ka type ka krna hoga IPD mai use krne sai phle
		
		// 1️. Fetch patient & hospital by externalId
//        Patient patient = patientRepository.findByExternalId(request.getPatientExternalId())
//                .orElseThrow(() -> new RuntimeException("Patient not found")); //Ye line work kre aur humare pass ye error(Type mismatch: cannot convert from Optional<Patient> to Patient) na aye isiliye hum patient repo mai Optional method banenge! 
//        
//        Hospital hospital = hospitalRepository.findByExternalId(request.getHospitalExternalId())
//                .orElseThrow(() -> new RuntimeException("Hospital not found")); //Ye line work kre aur humare pass ye error(Type mismatch: cannot convert from Optional<Hospital> to Hospital) na aye isiliye hum hospital repo mai Optional method banenge! 
        
        // 2️. Calculate days admitted
//        long daysAdmitted = ChronoUnit.DAYS.between(request.getAdmissionDate(), request.getDischargeDate()) + 1;
        long daysAdmitted = Math.max(1, ChronoUnit.DAYS.between(request.getAdmissionDate(), request.getDischargeDate()) + 1);

//        long days = ChronoUnit.DAYS.
//        if(daysAdmitted<0) daysAdmitted = 1; // minimum 1 day
        
        
        //3. Calculating total
        double roomCharges  = request.getRoomRatePerDay() * daysAdmitted;
        double medicationCharges = request.getMedicationCharges() * daysAdmitted;
        double total  = roomCharges + medicationCharges + request.getNursingCharges()+ 
        		request.getDoctorFee()+ request.getDiagnosticCharges()+
        		request.getFoodCharges()+ request.getMiscellaneousCharges();
        
        
        //4. Save The Billing Total 
        BillingMaster billingMaster = new BillingMaster();
        billingMaster.setHospitaExternallId(request.getHospitalExternalId());
        billingMaster.setPatientExternalId(request.getPatientExternalId());
        billingMaster.setAdmissionId(request.getAdmissionId());
        billingMaster.setModuleType("IPD");
//        billingMaster.setPaymentStatus(request.getPaymentStatus());
        billingMaster.setPaymentStatus(PaymentStatus.PENDING);
        billingMaster.setTotalAmount(total);
        billingMasterRepository.save(billingMaster);
		// TODO Auto-generated method stub
        
        
        //5. Save the Billing in IPD
        IPDBillingDetails details = new IPDBillingDetails();
        details.setBillingMaster(billingMaster);
        details.setAdmissionId(request.getAdmissionId());
        details.setRoomCharges(roomCharges);
        details.setDoctorFees(request.getDoctorFee());
        details.setMedicationCharges(request.getMedicationCharges());
        details.setNursingCharges(request.getNursingCharges());
        details.setDiagnosticCharges(request.getDiagnosticCharges());
//      details.setOtCharges(request.getOtCharges());
        details.setFoodCharges(request.getFoodCharges());
        details.setMiscellaneousCharges(request.getMiscellaneousCharges());
        details.setDaysAdmitted(daysAdmitted);
        details.setTotal(total);
        
		return ipdBillingRepository.save(details);
	}

	@Override
	public String processPayment(IpdPaymentRequestDTO request) {
		
		BillingMaster billingMaster = billingMasterRepository.findByAdmissionId(request.getAdmissionId())
				.orElseThrow(()-> new RuntimeException("Billing not found!"));
		
//	    if ("PAID".equalsIgnoreCase(billingMaster.getPaymentStatus())) {
//	        return "Payment already processed.";
//	    }
		
		billingMaster.setPaymentStatus(PaymentStatus.PAID);
		billingMaster.setPaymentMode(request.getPaymentMode());
//		billingMaster.set
		billingMasterRepository.save(billingMaster);
		
	       // 3️⃣ Call IPD API for Discharge
//     String dischargeApiUrl = "http://localhost:8181/api/ipd/discharge-patient?admissionId=" + request.getAdmissionId();
//     restTemplate.postForEntity(dischargeApiUrl, null, String.class);
     
//		return "Payment Done!";
		  return "Payment processed successfully for Admission ID: " + request.getAdmissionId();

	}

	@Override
	public IPDBillingDetails getBillingDetailsByAdmissionId(Long admissionId) {
		return ipdBillingRepository.findByAdmissionId(admissionId)
                .orElseThrow(() -> new RuntimeException("No billing found for admission ID: " + admissionId));
   
	}


	
	

}
