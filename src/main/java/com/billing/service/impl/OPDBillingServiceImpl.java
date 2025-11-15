package com.billing.service.impl;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

import com.billing.dto.BillingResponseDTO;
import com.billing.dto.OpdBillRequestDTO;
import com.billing.dto.OpdBillingDeatilsResponse;
import com.billing.dto.OpdPaymentRequestDTO;
import com.billing.enums.PaymentMode;
import com.billing.enums.PaymentStatus;
import com.billing.model.BillingMaster;
import com.billing.model.Hospital;
import com.billing.model.OPDBillingDetails;
import com.billing.model.Patient;
import com.billing.repository.BillingMasterRepository;
import com.billing.repository.HospitalRepository;
import com.billing.repository.OPDBillingRepository;
import com.billing.repository.PatientRepository;
import com.billing.service.OPDBillingService;

@Service
public class OPDBillingServiceImpl implements OPDBillingService
{
	
	@Autowired
	private BillingMasterRepository billingMasterRepository;
	
	@Autowired
	private OPDBillingRepository opdBillingRepository;
	
	
//	Method to Generate the Bill for patient present in OPD
	@Override
	public OPDBillingDetails generateOpdBilling(OpdBillRequestDTO request) {
		
		Double total = request.getDoctorFee() + request.getEmergencyFee() +
				       request.getDressing()  + request.getInjection()    + request.getMinorProcedure();
		
		BillingMaster billingMaster = new BillingMaster();
		billingMaster.setHospitaExternallId(request.getHospitalExternalId());
		billingMaster.setPatientExternalId(request.getPatientExternalId());
		billingMaster.setModuleType("OPD");
		billingMaster.setAppointmentId(request.getAppointmentId());
//		billingMaster.setPaymentStatus(PaymentStatus.PENDING);
		billingMaster.setTotalAmount(total);
		billingMaster.setPaymentStatus(PaymentStatus.PAID);
		billingMaster.setPaymentMode(request.getPaymentMode());
		billingMasterRepository.save(billingMaster);
		
		OPDBillingDetails details = new OPDBillingDetails();
		details.setAppointmentId(request.getAppointmentId());
		details.setBillingMaster(billingMaster);
		details.setDoctorId(request.getDoctorId());
		details.setDoctorFee(request.getDoctorFee());
		details.setDressing(request.getDressing());
		details.setEmergencyFee(request.getEmergencyFee());
		details.setInjection(request.getInjection());
		details.setMinorProcedure(request.getMinorProcedure());
		details.setTotal(total);
		details.setVisitDate(LocalDateTime.now());
		
		OPDBillingDetails billingResponse = opdBillingRepository.save(details);
		return billingResponse;
	}

	//Method to Process payment 
	@Override
	public String processPayment(OpdPaymentRequestDTO request) {
		BillingMaster billingMaster = billingMasterRepository.findByAppointmentId(request.getAppointmentId())
				.orElseThrow(()-> new ResourceAccessException("Billing not found for Appointment Id: " + request.getAppointmentId()));
		
		billingMaster.setPaymentStatus(PaymentStatus.PAID);
		billingMaster.setPaymentMode(request.getPaymentMode());
		billingMasterRepository.save(billingMaster);
		return "Payment processed successfully for Appointment ID: " + request.getAppointmentId();
	}

	
	//Method to Get the Billing details for OPD patient
	@Override
	public OpdBillingDeatilsResponse getBillingDetailsByAppoitmnetId(Long appointmentId) {
		OPDBillingDetails details = opdBillingRepository.findByAppointmentId(appointmentId)
				.orElseThrow(()-> new ResourceAccessException("Appoitment not found With Id: " + appointmentId));
		
		OpdBillingDeatilsResponse response = new OpdBillingDeatilsResponse();
		response.setId(details.getId());
		response.setDoctorFee(details.getDoctorFee());
		response.setDressing(details.getDressing());
		response.setEmergencyFee(details.getEmergencyFee());
		response.setInjection(details.getInjection());
		response.setMinorProcedure(details.getMinorProcedure());
		response.setTotal(details.getTotal());
		response.setVisitDate(details.getVisitDate());
		
		// ✅ Get billing master data from the entity relation
	    BillingMaster bmEntity = details.getBillingMaster();
		
		BillingResponseDTO bm = new BillingResponseDTO();
		bm.setId(bmEntity.getId());
		bm.setAppointmentId(bmEntity.getAppointmentId());
		bm.setPaymentStatus(bmEntity.getPaymentStatus());
		bm.setPaymentMode(bmEntity.getPaymentMode());
		bm.setHospitaExternallId(bmEntity.getHospitaExternallId());
		bm.setPatientExternalId(bmEntity.getPatientExternalId());
		
		response.setBillingResponseDTO(bm);
		return response;
	}
	
	
	
	
	

	
	
	
	
}
