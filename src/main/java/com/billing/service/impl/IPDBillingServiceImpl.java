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

import jakarta.transaction.Transactional;

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
        details.setServiceCharges(0.0);
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
    
    
//    Update the billing
    @Override
    @Transactional
    public IPDBillingDetails updateIpdBill(IpdBillUpdateRequestDTO request) {
        IPDBillingDetails billing = ipdBillingRepository.findByAdmissionId(request.getAdmissionId())
                .orElseThrow(() -> new RuntimeException("No billing found for admission ID: " + request.getAdmissionId()));

        long daysAdmitted = Math.max(1, ChronoUnit.DAYS.between(request.getAdmissionDate(), request.getDischargeDate()) + 1);

        // === RECALCULATE ALL DAILY CHARGES FRESH ===
        double roomCharges = request.getRoomRatePerDay() * daysAdmitted;

        double nursingCharges = request.getNursingChargesPerDay() * daysAdmitted;
        double foodCharges = request.getFoodChargesPerDay() * daysAdmitted;
        double diagnosticCharges = request.getDiagnosticChargesPerDay() * daysAdmitted;
        double miscDailyCharges = request.getMiscChargesPerDay() * daysAdmitted;

        // === USE LATEST ACCUMULATED VALUES FROM REQUEST (NOT OLD DB VALUES!) ===
        double medicationCharges = request.getMedicationCharges();
        double doctorFees = request.getDoctorFee();                    // Now correct × days
        double procedureCharges = request.getProcedureCharges();
        double extraServiceCharges = request.getExtraServiceCharges(); // nursing + food + diag + misc extras

        double totalMisc = miscDailyCharges + extraServiceCharges;
        
     // === IMPORTANT: ADD EXISTING SERVICE CHARGES ===
        double existingServiceCharges =
                billing.getServiceCharges() != null ? billing.getServiceCharges() : 0.0;

        double totalBeforeDiscount = roomCharges +
                medicationCharges +
                doctorFees +
                nursingCharges +
                diagnosticCharges +
                procedureCharges +
                foodCharges +
                totalMisc +
                existingServiceCharges;;

        double discountAmount = totalBeforeDiscount * (request.getDiscountPercentage() / 100.0);
        double totalAfterDiscount = totalBeforeDiscount - discountAmount;
        double gstAmount = totalAfterDiscount * (request.getGstPercentage() / 100.0);
        double finalTotal = totalAfterDiscount + gstAmount;

        // === UPDATE ALL FIELDS WITH FRESH VALUES ===
        billing.setDaysAdmitted(daysAdmitted);
        billing.setRoomCharges(roomCharges);
        billing.setMedicationCharges(medicationCharges);
        billing.setDoctorFees(doctorFees);
        billing.setNursingCharges(nursingCharges);
        billing.setDiagnosticCharges(diagnosticCharges);
        billing.setProcedureCharges(procedureCharges);
        billing.setFoodCharges(foodCharges);
        billing.setMiscellaneousCharges(totalMisc);

        billing.setTotalBeforeDiscount(totalBeforeDiscount);
        billing.setDiscountAmount(discountAmount);
        billing.setTotalAfterDiscountAndGst(totalAfterDiscount);
        billing.setGstAmount(gstAmount);
        billing.setTotal(finalTotal);
        billing.setDiscountPercentage(request.getDiscountPercentage());
        billing.setGstPercentage(request.getGstPercentage());

        // Update master total
        BillingMaster master = billing.getBillingMaster();
        if (master != null) {
            master.setTotalAmount(finalTotal);
            billingMasterRepository.save(master);
        }

        return ipdBillingRepository.save(billing);
    }

    /* --------------------------------------------------------------
       2. GRANULAR CHARGES (services / doctor visits / medicines)
       -------------------------------------------------------------- */
    @Override
    @Transactional
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

        double newServiceTotal = request.getServices().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();

        // Update service charges
        double currentServiceCharges = billing.getServiceCharges() != null ? billing.getServiceCharges() : 0.0;
        billing.setServiceCharges(currentServiceCharges + newServiceTotal);

        // Re-calculate total
        double oldTotalBeforeDiscount = billing.getTotalBeforeDiscount();
        double newTotalBeforeDiscount = oldTotalBeforeDiscount + newServiceTotal;

        double discountAmount = newTotalBeforeDiscount * (billing.getDiscountPercentage() / 100.0);
        double totalAfterDiscount = newTotalBeforeDiscount - discountAmount;
        double gstAmount = totalAfterDiscount * (billing.getGstPercentage() / 100.0);
        double finalTotal = totalAfterDiscount + gstAmount;

        billing.setTotalBeforeDiscount(newTotalBeforeDiscount);
        billing.setDiscountAmount(discountAmount);
        billing.setTotalAfterDiscountAndGst(totalAfterDiscount);
        billing.setGstAmount(gstAmount);
        billing.setTotal(finalTotal);

        // Update BillingMaster
        BillingMaster master = billing.getBillingMaster();
        if (master != null) {
            master.setTotalAmount(finalTotal);
            billingMasterRepository.save(master);
        }

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


//	@Override
//	public IPDBillingDetails updateIpdBIll(IpdBillRequestDTO request) {
//		// TODO Auto-generated method stub
//		return null;
//	}
}