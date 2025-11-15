package com.billing.service.impl;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.billing.dto.*;
import com.billing.enums.PaymentStatus;
import com.billing.model.*;
import com.billing.repository.*;
import com.billing.service.IPDBillingService;

@Service
public class IPDBillingServiceImpl implements IPDBillingService {

    @Autowired private IPDBillingRepository ipdBillingRepository;
    @Autowired private BillingMasterRepository billingMasterRepository;
    @Autowired private IPDServiceUsageRepository ipdServiceUsageRepository;
    @Autowired private IPDDoctorVisitRepository ipdDoctorVisitRepository;
    @Autowired private IPDMedicationRepository ipdMedicationRepository;

    /* --------------------------------------------------------------
       1. CORE BILLING (unchanged – copy-paste from your old file)
       -------------------------------------------------------------- */
    @Override
    public IPDBillingDetails generateIpdBill(IpdBillRequestDTO request) {
        long daysAdmitted = Math.max(1,
                ChronoUnit.DAYS.between(request.getAdmissionDate(),
                        request.getDischargeDate()) + 1);

        double roomCharges          = request.getRoomRatePerDay() * daysAdmitted;
        double medicationCharges    = request.getMedicationCharges();
        double doctorFees           = request.getDoctorFee();
        double nursingCharges       = request.getNursingCharges();
        double diagnosticCharges    = request.getDiagnosticCharges();
        double procedureCharges     = request.getProcedureCharges();
        double foodCharges          = request.getFoodCharges();
        double miscCharges          = request.getMiscellaneousCharges();

        double totalBeforeDiscount = roomCharges + medicationCharges + doctorFees + nursingCharges +
                diagnosticCharges + procedureCharges + foodCharges + miscCharges;

        double discountAmount      = totalBeforeDiscount * (request.getDiscountPercentage() / 100);
        double totalAfterDiscount  = totalBeforeDiscount - discountAmount;

        double gstAmount           = totalAfterDiscount * (request.getGstPercentage() / 100);
        double finalTotal          = totalAfterDiscount + gstAmount;

        // ----- BillingMaster -----
        BillingMaster billingMaster = new BillingMaster();
        billingMaster.setHospitaExternallId(request.getHospitalExternalId());
        billingMaster.setPatientExternalId(request.getPatientExternalId());
        billingMaster.setAdmissionId(request.getAdmissionId());
        billingMaster.setModuleType("IPD");
        billingMaster.setPaymentStatus(PaymentStatus.PENDING);
        billingMaster.setTotalAmount(finalTotal);
        billingMasterRepository.save(billingMaster);

        // ----- IPDBillingDetails -----
        IPDBillingDetails details = new IPDBillingDetails();
        details.setBillingMaster(billingMaster);
        details.setAdmissionId(request.getAdmissionId());
        details.setRoomCharges(roomCharges);
        details.setMedicationCharges(medicationCharges);
        details.setDoctorFees(doctorFees);
        details.setNursingCharges(nursingCharges);
        details.setDiagnosticCharges(diagnosticCharges);
        details.setProcedureCharges(procedureCharges);
        details.setFoodCharges(foodCharges);
        details.setMiscellaneousCharges(miscCharges);
        details.setDaysAdmitted(daysAdmitted);
        details.setTotalBeforeDiscount(totalBeforeDiscount);
        details.setDiscountPercentage(request.getDiscountPercentage());
        details.setDiscountAmount(discountAmount);
        details.setTotalAfterDiscountAndGst(totalAfterDiscount);
        details.setGstPercentage(request.getGstPercentage());
        details.setGstAmount(gstAmount);
        details.setTotal(finalTotal);

        return ipdBillingRepository.save(details);
    }


    @Override
    public String processPayment(IpdPaymentRequestDTO request) {
        BillingMaster billingMaster = billingMasterRepository.findByAdmissionId(request.getAdmissionId())
                .orElseThrow(() -> new RuntimeException("Billing not found!"));

        billingMaster.setPaymentStatus(PaymentStatus.PAID);
        billingMaster.setPaymentMode(request.getPaymentMode());
        billingMasterRepository.save(billingMaster);

        return "Payment processed successfully for Admission ID: " + request.getAdmissionId();
    }

    @Override
    public IpdBillingDetailsResponse getBillingDetailsByAdmissionId(Long admissionId) {
        IPDBillingDetails details = ipdBillingRepository.findByAdmissionId(admissionId)
                .orElseThrow(() -> new RuntimeException("No billing found for admission ID: " + admissionId));
        
       List<IPDServiceUsage> serviceList=  ipdServiceUsageRepository.findByIpdBillingDetailsId(details.getId());
        
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
        response.setIpdServices(serviceList);
        response.setDiscountAmount(details.getDiscountAmount());
        response.setDiscountPercentage(details.getDiscountPercentage());
        response.setGstAmount(details.getGstAmount());
        response.setGstPercentage(details.getGstPercentage());
        response.setTotalBeforeDiscount(details.getTotalBeforeDiscount());
        response.setTotalAfterDiscountAndGst(details.getTotalAfterDiscountAndGst());

        IpdBillingDetailsResponse.BillingMasterDTO bm = new IpdBillingDetailsResponse.BillingMasterDTO();
        bm.setId(details.getBillingMaster().getId());
        bm.setHospitaExternallId(details.getBillingMaster().getHospitaExternallId());
        bm.setPatientExternalId(details.getBillingMaster().getPatientExternalId());
        bm.setAdmissionId(details.getBillingMaster().getAdmissionId());
        bm.setModuleType(details.getBillingMaster().getModuleType());
        bm.setTotalAmount(details.getBillingMaster().getTotalAmount());
        bm.setPaymentStatus(details.getBillingMaster().getPaymentStatus().name());
        bm.setPaymentMode(details.getBillingMaster().getPaymentMode() != null
                ? details.getBillingMaster().getPaymentMode().name() : null);
        bm.setBillingDate(details.getBillingMaster().getBillingDate());
        response.setBillingMaster(bm);

        return response;
    }

    /* --------------------------------------------------------------
       2. GRANULAR CHARGES (services / doctor visits / medicines)
       -------------------------------------------------------------- */
    @Override
    public List<IPDServiceUsage> addServices(AddServicesRequest request) {
    	
        IPDBillingDetails billing = ipdBillingRepository.findById(request.getIpdBillingId())
                .orElseThrow(() -> new RuntimeException("IPD Billing not found"));
        

        List<IPDServiceUsage> usages = request.getServices().stream().map(item -> {
           Double total = item.getPrice() * item.getQuantity();
            return IPDServiceUsage.builder()
                    .ipdBillingDetails(billing)
                    .serviceName(item.getServiceName())
                    .price(item.getPrice())
                    .quantity(item.getQuantity())
                    .totalAmount(total)
                    .serviceAddDate(LocalDateTime.now())
                    .build();
        }).toList();
        
        Double totalServiceCharges = request.getServices().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
        
        billing.setServiceCharges(billing.getServiceCharges() + totalServiceCharges );
        billing.setTotal(billing.getTotal() + totalServiceCharges);
        ipdBillingRepository.save(billing);

        return ipdServiceUsageRepository.saveAll(usages);
    }

    @Override
    public List<IPDDoctorVisit> addDoctorVisits(AddDoctorVisitsRequest request) {
        IPDBillingDetails billing = ipdBillingRepository.findById(request.getIpdBillingId())
                .orElseThrow(() -> new RuntimeException("IPD Billing not found"));

        List<IPDDoctorVisit> visits = request.getVisits().stream()
                .map(v -> IPDDoctorVisit.builder()
                        .ipdBillingDetails(billing)
                        .visitDate(v.getVisitDate())
                        .doctorName(v.getDoctorName())
                        .consultationFee(v.getConsultationFee())
                        .build())
                .toList();

        return ipdDoctorVisitRepository.saveAll(visits);
    }

    @Override
    public List<IPDMedication> addMedications(AddMedicationsRequest request) {
        IPDBillingDetails billing = ipdBillingRepository.findById(request.getIpdBillingId())
                .orElseThrow(() -> new RuntimeException("IPD Billing not found"));

        List<IPDMedication> meds = request.getMedications().stream().map(m -> {
            Double total = m.getPricePerUnit() * m.getQuantity();
            return IPDMedication.builder()
                    .ipdBillingDetails(billing)
                    .medicineName(m.getMedicineName())
                    .quantity(m.getQuantity())
                    .pricePerUnit(m.getPricePerUnit())
                    .totalPrice(total)
                    .dosage(m.getDosage())
                    .build();
        }).toList();

        return ipdMedicationRepository.saveAll(meds);
    }

    @Override
    public List<IPDServiceUsage> getServicesByBillingId(Long ipdBillingId) {
        return ipdServiceUsageRepository.findByIpdBillingDetailsId(ipdBillingId);
    }

    @Override
    public List<IPDDoctorVisit> getDoctorVisitsByBillingId(Long ipdBillingId) {
        return ipdDoctorVisitRepository.findByIpdBillingDetailsId(ipdBillingId);
    }

    @Override
    public List<IPDMedication> getMedicationsByBillingId(Long ipdBillingId) {
        return ipdMedicationRepository.findByIpdBillingDetailsId(ipdBillingId);
    }
}