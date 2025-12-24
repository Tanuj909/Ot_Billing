package com.billing.service.impl;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.billing.dto.*;
import com.billing.dto.AddServicesRequest.ServiceItem;
import com.billing.enums.PaymentMode;
import com.billing.enums.PaymentStatus;
import com.billing.model.*;
import com.billing.repository.*;
import com.billing.service.IPDBillingService;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IPDBillingServiceImpl implements IPDBillingService {

    @Autowired private IPDBillingRepository ipdBillingRepository;
    @Autowired private BillingMasterRepository billingMasterRepository;
    @Autowired private IPDServiceUsageRepository ipdServiceUsageRepository;
    @Autowired private IPDDoctorVisitRepository ipdDoctorVisitRepository;
    @Autowired private IPDMedicationRepository ipdMedicationRepository;
    @Autowired private IpdPaymentHistoryRepository ipdPaymentHistoryRepository;

    //->Round to nearest whole Rupee/Value
    private double round0(double value) {
    	return Math.round(value); // round to nearest whole rupee
    }

//----------------------------------------Generate IPD Billing-------------------------------------------------//    
    @Override
    @Transactional
    public IPDBillingDetails generateIpdBill(IpdBillRequestDTO request) {
        Long admissionId = request.getAdmissionId();

        if (ipdBillingRepository.findByAdmissionId(admissionId).isPresent()) {
            throw new IllegalStateException("Bill already generated for admission ID: " + admissionId);
        }

        long daysAdmitted = Math.max(1, ChronoUnit.DAYS.between(request.getAdmissionDate(), request.getDischargeDate()) + 1);

        double roomCharges = request.getRoomRatePerDay() * daysAdmitted;
        double medicationCharges = nullToZero(request.getMedicationCharges());
        double doctorFees = nullToZero(request.getDoctorFee());
        double nursingCharges = nullToZero(request.getNursingCharges());
        double diagnosticCharges = nullToZero(request.getDiagnosticCharges());
        double procedureCharges = nullToZero(request.getProcedureCharges());
        double foodCharges = nullToZero(request.getFoodCharges());
        double miscCharges = nullToZero(request.getMiscellaneousCharges());

        double totalBeforeDiscount = roomCharges + medicationCharges + doctorFees + nursingCharges +
                                    diagnosticCharges + procedureCharges + foodCharges + miscCharges;

        double discountPercentage = nullToZero(request.getDiscountPercentage());
        double discountAmount = round0(totalBeforeDiscount * discountPercentage / 100.0);
        double totalAfterDiscount = totalBeforeDiscount - discountAmount;

        // Initial GST = 0 (will be added per-item later)
        double gstAmount = 0.0;
        double finalTotal = round0(totalAfterDiscount + gstAmount);

        Double advancePaid = nullToZero(request.getAdvanceAmount());

        // BillingMaster
        BillingMaster billingMaster = BillingMaster.builder()
                .hospitaExternallId(request.getHospitalExternalId())
                .patientExternalId(request.getPatientExternalId())
                .admissionId(request.getAdmissionId())
                .moduleType("IPD")
                .totalAmount(finalTotal)
                .paymentStatus(PaymentStatus.PENDING)
                .advancePaymentMode(request.getAdvancePaymentMode())
                .billingDate(LocalDateTime.now())
                .build();
        billingMasterRepository.save(billingMaster);

        // IPDBillingDetails
        IPDBillingDetails details = new IPDBillingDetails();
        details.setBillingMaster(billingMaster);
        details.setAdmissionId(request.getAdmissionId());
        details.setDaysAdmitted(daysAdmitted);

        details.setRoomCharges(roomCharges);
        details.setMedicationCharges(medicationCharges);
        details.setDoctorFees(doctorFees);
        details.setNursingCharges(nursingCharges);
        details.setDiagnosticCharges(diagnosticCharges);
        details.setProcedureCharges(procedureCharges);
        details.setFoodCharges(foodCharges);
        details.setMiscellaneousCharges(miscCharges);
        details.setServiceCharges(0.0);

        details.setTotalBeforeDiscount(round0(totalBeforeDiscount));
        details.setDiscountPercentage(discountPercentage);
        details.setDiscountAmount(discountAmount);
        details.setGstPercentage(0.0); // fallback
        details.setGstAmount(0.0);
        details.setTotalItemGstAmount(0.0);

        details.setTotalAfterDiscountAndGst(finalTotal);
        details.setTotal(finalTotal);

        details.setAdvancePaid(advancePaid);
        details.setTotalPayments(0.0);
        details.setTotalCharges(finalTotal);
        details.setDueAmount(round0(finalTotal - advancePaid));
        details.setBillingStatus("ACTIVE");

        return ipdBillingRepository.save(details);
    }

 
//----------------------------------------Update IPD Billing-------------------------------------------------//     
    @Override
    @Transactional
    public IPDBillingDetails updateIpdBill(IpdBillUpdateRequestDTO request) {
        IPDBillingDetails billing = ipdBillingRepository.findByAdmissionId(request.getAdmissionId())
                .orElseThrow(() -> new RuntimeException("No billing found for admission ID: " + request.getAdmissionId()));

        // Prevent update if bill is already CLOSED
        if ("CLOSED".equals(billing.getBillingStatus())) {
            throw new IllegalStateException("Cannot update a CLOSED bill for admission ID: " + request.getAdmissionId());
        }

        long daysAdmitted = Math.max(1, ChronoUnit.DAYS.between(request.getAdmissionDate(), request.getDischargeDate()) + 1);

        // === DAILY CHARGES (scaled by days) ===
        double roomCharges = nullToZero(request.getRoomRatePerDay()) * daysAdmitted;
        double nursingCharges = nullToZero(request.getNursingChargesPerDay()) * daysAdmitted;
        double foodCharges = nullToZero(request.getFoodChargesPerDay()) * daysAdmitted;
        double diagnosticCharges = nullToZero(request.getDiagnosticChargesPerDay()) * daysAdmitted;

        // === ONE-TIME / ACCUMULATED CHARGES (from request) ===
        double medicationCharges = nullToZero(request.getMedicationCharges());
        double doctorFees = nullToZero(request.getDoctorFee());
        double procedureCharges = nullToZero(request.getProcedureCharges());

        // Pre-calculated miscellaneous from IPD module
        double totalMisc = nullToZero(request.getMiscellaneousCharges());

        // === PRESERVE EXISTING SERVICE CHARGES (from addServices – lab, physio, etc.) ===
        double existingServiceCharges = nullToZero(billing.getServiceCharges());

        // === NEW TOTAL BEFORE DISCOUNT ===
        double totalBeforeDiscount = roomCharges +
                                     medicationCharges +
                                     doctorFees +
                                     nursingCharges +
                                     diagnosticCharges +
                                     procedureCharges +
                                     foodCharges +
                                     totalMisc +
                                     existingServiceCharges;

        billing.setTotalBeforeDiscount(round0(totalBeforeDiscount));

        // === DISCOUNT ===
        double discountPercentage = nullToZero(request.getDiscountPercentage());
        double discountAmount = round0(totalBeforeDiscount * discountPercentage / 100.0);
        billing.setDiscountAmount(discountAmount);
        billing.setDiscountPercentage(discountPercentage);

        double totalAfterDiscount = totalBeforeDiscount - discountAmount;

        // === GST: PRESERVE PER-ITEM GST (DO NOT RECALCULATE USING gstPercentage) ===
        double totalGst = nullToZero(billing.getTotalItemGstAmount());  // Comes from addServices only

        billing.setGstAmount(round0(totalGst));  // Keep for backward compatibility
        // Do NOT use request.getGstPercentage() here — it's a fallback only

        // === FINAL TOTAL ===
        double finalTotal = round0(totalAfterDiscount + totalGst);
        billing.setTotalAfterDiscountAndGst(finalTotal);
        billing.setTotal(finalTotal);

        // === UPDATE ALL CHARGE FIELDS ===
        billing.setDaysAdmitted(daysAdmitted);
        billing.setRoomCharges(round0(roomCharges));
        billing.setMedicationCharges(round0(medicationCharges));
        billing.setDoctorFees(round0(doctorFees));
        billing.setNursingCharges(round0(nursingCharges));
        billing.setDiagnosticCharges(round0(diagnosticCharges));
        billing.setProcedureCharges(round0(procedureCharges));
        billing.setFoodCharges(round0(foodCharges));
        billing.setMiscellaneousCharges(round0(totalMisc));

        // === NEW FLOW FIELDS ===
        billing.setTotalCharges(finalTotal);

        double advancePaid = nullToZero(billing.getAdvancePaid());
        double totalPayments = nullToZero(billing.getTotalPayments());
        double newDueAmount = round0(finalTotal - (advancePaid + totalPayments));
        billing.setDueAmount(newDueAmount);

        billing.setBillingStatus("ACTIVE");
        billing.setUpdatedAt(LocalDateTime.now());

        // === Update BillingMaster ===
        BillingMaster master = billing.getBillingMaster();
        if (master != null) {
            master.setTotalAmount(finalTotal);
            billingMasterRepository.save(master);
        }

        return ipdBillingRepository.save(billing);
    }
    
    private Double nullToZero(Double value) {
        return value != null ? value : 0.0;
    }
    
    
    
//----------------------------------------Close IPD Billing-------------------------------------------------//    
    @Override
    @Transactional
    public void closeBillOnDischarge(Long admissionId) {

        BillingMaster billingMaster = billingMasterRepository.findByAdmissionId(admissionId)
                .orElseThrow(() -> new RuntimeException("Billing not found!"));

        IPDBillingDetails billing = ipdBillingRepository.findByAdmissionId(admissionId)
                .orElseThrow(() -> new RuntimeException("Billing Details not found!"));

        // Cannot close bill with pending due
        if (billing.getDueAmount() > 0) {
            throw new IllegalStateException("Cannot close bill. Pending due exists.");
        }

        // Mark final payment status
        billingMaster.setPaymentStatus(PaymentStatus.PAID);

        // Mark bill inactive (discharged)
        billing.setBillingStatus("INACTIVE");

        billingMasterRepository.save(billingMaster);
        ipdBillingRepository.save(billing);
    }

    
//----------------------------------------Partial IPD Payment-------------------------------------------------//    
    @Override
    @Transactional
    public void makePartialPayment(IpdPartialPaymentRequestDTO request) {
        IPDBillingDetails billing = ipdBillingRepository.findByAdmissionId(request.getAdmissionId())
            .orElseThrow(() -> new RuntimeException("Billing not found for admission ID: " + request.getAdmissionId()));
//
        
//        BillingMaster master = billingMasterRepository.findByAdmissionId(request.getAdmissionId())
//        		.orElseThrow(()-> new RuntimeException("Billing Master not Found" + request.getAdmissionId()));
        		
        // Only block if patient already discharged
        if ("CLOSED".equals(billing.getBillingStatus())) {
            throw new IllegalStateException("Patient is already discharged. Cannot accept payment.");
        }

        double amount = request.getAmount();
        if (amount <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }

        double currentDue = billing.getDueAmount();
        if (amount > currentDue && currentDue > 0.01) {
            throw new IllegalArgumentException(
                "Amount exceeds current due of ₹" + String.format("%.2f", currentDue)
            );
        }
        
     // === RECORD PAYMENT IN HISTORY ===
        IpdPaymentHistory history = IpdPaymentHistory.builder()
            .admissionId(request.getAdmissionId())
            .amount(amount)
            .paymentMode(request.getPaymentMode())
            .paymentDate(LocalDateTime.now())
            .paidBy("Counter Staff") // Can be enhanced
            .receiptNo("REC" + System.currentTimeMillis())
            .billingDetails(billing)
            .createdAt(LocalDateTime.now())   // THIS LINE WAS MISSING!
            .build();
        
     // Add to billing's history list
        if (billing.getPaymentHistory() == null) {
            billing.setPaymentHistory(new ArrayList<>());
        }
        billing.getPaymentHistory().add(history);

        // Increase totalPayments
        double newTotalPayments = (billing.getTotalPayments() != null ? billing.getTotalPayments() : 0.0) + amount;
        billing.setTotalPayments(newTotalPayments);

        // Recalculate due amount (allow negative = credit)
        double newDue = billing.getTotalCharges() - (billing.getAdvancePaid() + newTotalPayments);
        billing.setDueAmount(newDue);
        
        // DO NOT CLOSE BILL HERE
        // DO NOT SET PAID STATUS
        // Bill stays ACTIVE until discharge

        // Optional: Update last payment mode in master (for receipt)
        BillingMaster master = billing.getBillingMaster();
        if (master != null) {
            master.setPaymentMode(PaymentMode.valueOf(request.getPaymentMode()));
            billingMasterRepository.save(master);
        }
        
        // === UPDATE PAYMENT STATUS BASED ON NEW DUE ===
        if (master != null) {
            if (newDue <= 0.0) {
                master.setPaymentStatus(PaymentStatus.PAID);
            } else if (newTotalPayments > 0) {
                master.setPaymentStatus(PaymentStatus.PARTIAL);
            } else {
                master.setPaymentStatus(PaymentStatus.PENDING);
            }
        }

        // Always recalculate (in case of floating point drift)
        billing.recalculateDueAmount();

        ipdPaymentHistoryRepository.save(history);
        ipdBillingRepository.save(billing);
    }

    
//----------------------------------------Get IPD Billing-------------------------------------------------//    
    @Override
    @Transactional
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
        response.setAdvanceAmount(details.getAdvancePaid());
        response.setDueAmmount(details.getDueAmount());
        response.setTotalPayedAmmount(details.getTotalPayments());
        response.setBillingStatus(details.getBillingStatus());

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
        bm.setAdvancePaymentMode(details.getBillingMaster().getAdvancePaymentMode());
        bm.setBillingDate(details.getBillingMaster().getBillingDate());
        response.setBillingMaster(bm);

        return response;
    }
    

    /* --------------------------------------------------------------
       2. GRANULAR CHARGES (services / doctor visits / medicines)
       -------------------------------------------------------------- */
    
//----------------------------------------Add Services to IPD Billing-------------------------------------------------//    
    @Override
    @Transactional
    public List<IPDServiceUsage> addServices(AddServicesRequest request) {
        IPDBillingDetails billing = ipdBillingRepository.findById(request.getIpdBillingId())
                .orElseThrow(() -> new RuntimeException("IPD Billing not found"));

        List<IPDServiceUsage> newUsages = new ArrayList<>();
        double newServiceTotalBeforeGst = 0.0;
        double newGstFromServices = 0.0;

        for (ServiceItem item : request.getServices()) {
            Double itemTotal = item.getPrice() * item.getQuantity();
            Double gstPercentage = item.getGstPercentage() != null ? item.getGstPercentage() : 0.0;
            Double gstAmount = round0((itemTotal * gstPercentage) / 100.0);

            newServiceTotalBeforeGst += itemTotal;
            newGstFromServices += gstAmount;

            IPDServiceUsage usage = IPDServiceUsage.builder()
                    .ipdBillingDetails(billing)
                    .serviceName(item.getServiceName())
                    .price(item.getPrice())
                    .quantity(item.getQuantity())
                    .totalAmount(itemTotal)
                    .gstPercentage(gstPercentage)
                    .gstAmount(gstAmount)
                    .serviceAddDate(LocalDateTime.now())
                    .build();

            newUsages.add(usage);
        }

        // Save services
        List<IPDServiceUsage> savedUsages = ipdServiceUsageRepository.saveAll(newUsages);
        billing.getServices().addAll(savedUsages);

        // Update serviceCharges (base amount only)
        double currentServiceCharges = nullToZero(billing.getServiceCharges());
        billing.setServiceCharges(currentServiceCharges + newServiceTotalBeforeGst);

        // === FULL RECALCULATION ===
        double totalDoctorFees = billing.getDoctorVisits().stream()
                .mapToDouble(v -> nullToZero(v.getConsultationFee()))
                .sum();

        double baseFixedCharges = totalDoctorFees +
                                  nullToZero(billing.getRoomCharges()) +
                                  nullToZero(billing.getMedicationCharges()) +
                                  nullToZero(billing.getNursingCharges()) +
                                  nullToZero(billing.getDiagnosticCharges()) +
                                  nullToZero(billing.getProcedureCharges()) +
                                  nullToZero(billing.getFoodCharges()) +
                                  nullToZero(billing.getMiscellaneousCharges()) +
                                  nullToZero(billing.getOtCharges());

        double serviceCharges = nullToZero(billing.getServiceCharges());

        double totalBeforeDiscount = baseFixedCharges + serviceCharges;
        billing.setTotalBeforeDiscount(round0(totalBeforeDiscount));

        // Discount
        double discountPercentage = nullToZero(billing.getDiscountPercentage());
        double discountAmount = round0(totalBeforeDiscount * discountPercentage / 100.0);
        billing.setDiscountAmount(discountAmount);

        double totalAfterDiscount = totalBeforeDiscount - discountAmount;

        // Total GST = previous + new from services
        double previousGst = nullToZero(billing.getTotalItemGstAmount());
        double totalGst = previousGst + newGstFromServices;
        billing.setTotalItemGstAmount(round0(totalGst));
        billing.setGstAmount(round0(totalGst)); // backward compatibility

        // Final total
        double finalTotal = round0(totalAfterDiscount + totalGst);
        billing.setTotalAfterDiscountAndGst(finalTotal);
        billing.setTotal(finalTotal);

        // Update totalCharges & due
        billing.setTotalCharges(finalTotal);
        double totalPaid = nullToZero(billing.getAdvancePaid()) + nullToZero(billing.getTotalPayments());
        billing.setDueAmount(round0(finalTotal - totalPaid));

        billing.setUpdatedAt(LocalDateTime.now());
        ipdBillingRepository.save(billing);

        // Update BillingMaster
        BillingMaster master = billing.getBillingMaster();
        if (master != null) {
            master.setTotalAmount(finalTotal);
            billingMasterRepository.save(master);
        }

        return savedUsages;
    }
    
    
//----------------------------------------Add Doctor Visits to IPD Billing-------------------------------------------------//
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

    
//----------------------------------------Add Medications to IPD Billing-------------------------------------------------//
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

    
//----------------------------------------Get Services By IPD Billing ID-------------------------------------------------//
    @Override
    public List<IPDServiceUsage> getServicesByBillingId(Long ipdBillingId) {
        return ipdServiceUsageRepository.findByIpdBillingDetailsId(ipdBillingId);
    }

//----------------------------------------Get Doctor Visits By IPD Billing ID-------------------------------------------------//
    @Override
    public List<IPDDoctorVisit> getDoctorVisitsByBillingId(Long ipdBillingId) {
        return ipdDoctorVisitRepository.findByIpdBillingDetailsId(ipdBillingId);
    }

//----------------------------------------Get Medications By IPD Billing ID-------------------------------------------------//
    @Override
    public List<IPDMedication> getMedicationsByBillingId(Long ipdBillingId) {
        return ipdMedicationRepository.findByIpdBillingDetailsId(ipdBillingId);
    }


    /* --------------------------------------------------------------
     Special Discount
    -------------------------------------------------------------- */
    
    @Override
    @Transactional
    public IPDBillingDetails specialDiscounts(SpecialDiscountRequestDTO request) {
    	
    	IPDBillingDetails billing = ipdBillingRepository.findByAdmissionId(request.getAdmissionId())
    	        .orElseThrow(() -> new RuntimeException("IPD Billing not found for admission ID: " + request.getAdmissionId()));
    	
    	// Only allow on ACTIVE bills (not closed/discharged)
        if (!"ACTIVE".equals(billing.getBillingStatus())) {
            throw new IllegalStateException("Special discount can only be applied on ACTIVE bills.");
        }
        
        //Getting the Final Amount which Due Amount!
        Double currentDue = nullToZero(billing.getDueAmount());
        if (currentDue <= 0.01) {
            throw new IllegalStateException("No pending due amount. Cannot apply special discount.");
        }
        
        Double percentage = request.getSpecialDiscountPercentage();
        if (percentage == null || percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Special discount percentage must be between 0 and 100.");
        }
        
        //Calculating the Special Discount!
        Double discountAmount = round0((currentDue*percentage)/100);
        
        //Calculating new due amount
        Double newDue = round0(currentDue - discountAmount);
        
        //Saving the Special Discount Percentage & Discount Amount!
        billing.setDueAfterSpecialDiscount(newDue);  //-> A new filed b/c update affects the existing due!
        billing.setSpecialDiscountPercentage(percentage);
        billing.setSpecialDiscountAmount(discountAmount);
        
    	return ipdBillingRepository.save(billing);  
    	}
    
}