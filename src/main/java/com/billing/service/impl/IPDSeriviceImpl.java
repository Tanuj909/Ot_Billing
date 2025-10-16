package com.billing.service.impl;

import java.time.temporal.ChronoUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.billing.dto.IpdBillRequestDTO;
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
public class IPDSeriviceImpl implements IPDBillingService{
	
	@Autowired
	private  IPDBillingRepository ipdBillingRepository;
	
	@Autowired
	private  BillingMasterRepository billingMasterRepository;
	
	@Autowired
	private  HospitalRepository hospitalRepository;
	
	@Autowired
	private  PatientRepository patientRepository;

	@Override
	public IPDBillingDetails generateIpdBill(IpdBillRequestDTO request) {
		
		 // 1️ Fetch patient & hospital by externalId
        Patient patient = patientRepository.findByExternalId(request.getPatientExternalId());
//                .orElseThrow(() -> new RuntimeException("Patient not found"));
        
        Hospital hospital = hospitalRepository.findByExternalId(request.getHospitalExternalId())
                .orElseThrow(() -> new RuntimeException("Hospital not found"));
        
        // 2️ Calculate days admitted
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
        billingMaster.setHospital(hospital);
        billingMaster.setPatient(patient);
        billingMaster.setModuleType("IPD");
        billingMaster.setTotalAmount(total);
        billingMasterRepository.save(billingMaster);
		// TODO Auto-generated method stub
        
        
        //5. Save the Billing in IPD
        IPDBillingDetails details = new IPDBillingDetails();
        details.setBillingMaster(billingMaster);
        details.setRoomCharges(roomCharges);
        details.setDoctorFees(request.getDoctorFee());
        details.setMedicationCharges(request.getMedicationCharges());
        details.setNursingCharges(request.getNursingCharges());
        details.setDiagnosticCharges(request.getDiagnosticCharges());
//        details.setOtCharges(request.getOtCharges());
        details.setFoodCharges(request.getFoodCharges());
        details.setMiscellaneousCharges(request.getMiscellaneousCharges());
        details.setDaysAdmitted(daysAdmitted);
        details.setTotal(total);
      
		return ipdBillingRepository.save(details);
	}
	

}
