package com.billing.service.impl;

import com.billing.dto.*;
import com.billing.enums.PaymentStatus;
import com.billing.model.BillingMaster;
import com.billing.model.OPDBillingDetails;
import com.billing.model.OPDServiceUsage;
import com.billing.repository.*;
import com.billing.service.OPDBillingService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OPDBillingServiceImpl implements OPDBillingService {

    @Autowired
    private BillingMasterRepository billingMasterRepository;

    @Autowired
    private OPDBillingRepository opdBillingRepository;

    @Autowired
    private OPDServiceUsageRepository opdServiceUsageRepository;

    // Generate initial bill (when appointment created & doctor fee paid)
    @Override
    public OPDBillingDetails generateOpdBilling(OpdBillRequestDTO request) {
        double total = request.getDoctorFee(); // Only doctor fee at start

        BillingMaster billingMaster = BillingMaster.builder()
                .hospitaExternallId(request.getHospitalExternalId())
                .patientExternalId(request.getPatientExternalId())
                .appointmentId(request.getAppointmentId())
                .moduleType("OPD")
                .totalAmount(total) // Initially = doctor fee
                .paymentStatus(PaymentStatus.PARTIAL)
                .paymentMode(request.getPaymentMode())
                .build();

        billingMaster = billingMasterRepository.save(billingMaster);

        OPDBillingDetails details = OPDBillingDetails.builder()
                .appointmentId(request.getAppointmentId())
                .doctorId(request.getDoctorId())
                .doctorFee(request.getDoctorFee())
                .serviceCharges(0.0)
                .totalFees(request.getDoctorFee())
                .payableAmount(0.0) // Nothing extra yet
                .visitDate(LocalDateTime.now())
                .billingMaster(billingMaster)
                .build();

        return opdBillingRepository.save(details);
    }

    @Override
	@Transactional
	public String addServicesToBilling(List<OpdServiceUsageRequestDTO> requests) {
	    if (requests.isEmpty()) return "No services provided";

	    Long appointmentId = requests.get(0).appointmentId();

	    BillingMaster billingMaster = billingMasterRepository.findByAppointmentId(appointmentId)
	    		.orElseThrow(()-> new ResourceAccessException("Billing not found for appointment: " + appointmentId));
//	        .orElseThrow(() -> new ResourceNotFoundException("Billing not found for appointment: " + appointmentId));

	    OPDBillingDetails details = opdBillingRepository.findByAppointmentId(appointmentId)
	    		.orElseThrow(()-> new ResourceAccessException("OPD Billing details not found"));
//	        .orElseThrow(() -> new ResourceNotFoundException("OPD Billing details not found"));

	    double totalServiceCharge = 0.0;

	    for (OpdServiceUsageRequestDTO req : requests) {
	        OPDServiceUsage usage = new OPDServiceUsage();
	        usage.setAppointmentId(req.appointmentId());
	        usage.setServiceName(req.serviceName());
	        usage.setServicePrice(req.servicePrice());
	        usage.setQuantity(req.quantity());
	        usage.setTotalPrice(req.servicePrice() * req.quantity());
	        usage.setBillingMaster(billingMaster);

	        opdServiceUsageRepository.save(usage);
	        totalServiceCharge += usage.getTotalPrice();
	    }

	    // Update OPDBillingDetails
	    details.setServiceCharges(details.getServiceCharges() + totalServiceCharge);
	    double newTotal = details.getServiceCharges() + details.getDoctorFee(); // Doctor fee already paid
	    details.setPayableAmount(Math.max(0, newTotal - details.getDoctorFee())); // Prevent negative
	    details.setTotalFees(newTotal);
	    
	    opdBillingRepository.save(details);
	    billingMaster.setTotalAmount(newTotal);
	    billingMasterRepository.save(billingMaster);

	    return "Services added. New amount to pay: " + details.getTotalFees();
	}

    // Final corrected response
    @Override
    public OpdBillingDeatilsResponse getBillingDetailsByAppointmentId(Long appointmentId) {
        OPDBillingDetails details = opdBillingRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceAccessException("Appointment not found with ID: " + appointmentId));

        double doctorFee = details.getDoctorFee() != null ? details.getDoctorFee() : 0.0;
        double serviceCharges = details.getServiceCharges() != null ? details.getServiceCharges() : 0.0;
        double totalFees = doctorFee + serviceCharges;
        double payableAmount = Math.max(0.0, totalFees - doctorFee);

        OpdBillingDeatilsResponse response = new OpdBillingDeatilsResponse();
        response.setId(details.getId());
        response.setDoctorFee(doctorFee);
        response.setServiceCharges(serviceCharges);
        response.setTotalFees(totalFees);
        response.setPayableAmount(payableAmount);
        response.setAmountToPay(payableAmount); // backward compat
        response.setVisitDate(details.getVisitDate());

        // Clean old fields
        response.setEmergencyFee(0.0);
        response.setDressing(0.0);
        response.setInjection(0.0);
        response.setMinorProcedure(0.0);

        BillingMaster bm = details.getBillingMaster();
        BillingResponseDTO bmDto = new BillingResponseDTO();
        bmDto.setId(bm.getId());
        bmDto.setAppointmentId(bm.getAppointmentId());
        bmDto.setHospitaExternallId(bm.getHospitaExternallId());
        bmDto.setPatientExternalId(bm.getPatientExternalId());
        bmDto.setPaymentStatus(bm.getPaymentStatus().name());
        bmDto.setPaymentMode(bm.getPaymentMode());
        bmDto.setTotalAmount(totalFees); // Now correct!
        bmDto.setModuleType("OPD");

        response.setBillingResponseDTO(bmDto);
        return response;
    }

    @Override
    public String processPayment(OpdPaymentRequestDTO request) {
        BillingMaster bm = billingMasterRepository.findByAppointmentId(request.getAppointmentId())
                .orElseThrow(() -> new ResourceAccessException("Billing not found"));
        bm.setPaymentStatus(PaymentStatus.PAID);
        bm.setPaymentMode(request.getPaymentMode());
        billingMasterRepository.save(bm);
        return "Payment successful for appointment " + request.getAppointmentId();
    }
    
    
    @Override
    public List<OpdServiceUsageResponseDTO> getAddedServicesByAppointmentId(Long appointmentId) {
        return opdServiceUsageRepository.findByAppointmentId(appointmentId)
                .stream()
                .map(usage -> {
                    OpdServiceUsageResponseDTO dto = new OpdServiceUsageResponseDTO();
                    dto.setId(usage.getId());
                    dto.setServiceName(usage.getServiceName());
                    dto.setServicePrice(usage.getServicePrice());
                    dto.setQuantity(usage.getQuantity());
                    dto.setTotalPrice(usage.getTotalPrice());
                    return dto;
                })
                .toList();
    }
    
}
