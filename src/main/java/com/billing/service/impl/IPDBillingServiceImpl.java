//package com.billing.service.impl;
//
//import java.time.LocalDateTime;
//import java.time.temporal.ChronoUnit;
//import java.util.ArrayList;
//import java.util.List;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import com.billing.dto.*;
//import com.billing.enums.PaymentMode;
//import com.billing.enums.PaymentStatus;
//import com.billing.model.*;
//import com.billing.repository.*;
//import com.billing.service.IPDBillingService;
//
//import jakarta.transaction.Transactional;
//
//@Service
//public class IPDBillingServiceImpl implements IPDBillingService {
//
//    @Autowired private IPDBillingRepository ipdBillingRepository;
//    @Autowired private BillingMasterRepository billingMasterRepository;
//    @Autowired private IPDServiceUsageRepository ipdServiceUsageRepository;
//    @Autowired private IPDDoctorVisitRepository ipdDoctorVisitRepository;
//    @Autowired private IPDMedicationRepository ipdMedicationRepository;
//    @Autowired private IpdPaymentHistoryRepository ipdPaymentHistoryRepository;
//
//    @Override
//    @Transactional
//    public CreateIpdBillingAccountResponse createBillingAccount(CreateIpdBillingAccountRequest request) {
//        Long admissionId = request.getAdmissionId();
//
//        // Prevent duplicate billing account
//        if (ipdBillingRepository.findByAdmissionId(admissionId).isPresent()) {
//            throw new IllegalStateException("Billing account already exists for admission ID: " + admissionId);
//        }
//
//        if (billingMasterRepository.findByAdmissionId(admissionId).isPresent()) {
//            throw new IllegalStateException("BillingMaster already exists for admission ID: " + admissionId);
//        }
//
//        Double advance = (request.getAdvanceAmount() != null && request.getAdvanceAmount() > 0)
//                ? request.getAdvanceAmount() : 0.0;
//
//        // Create BillingMaster
//        BillingMaster master = BillingMaster.builder()
//                .hospitaExternallId(request.getHospitalExternalId())
//                .patientExternalId(request.getPatientExternalId())
//                .admissionId(admissionId)
//                .moduleType("IPD")
//                .totalAmount(0.0)
//                .paymentStatus(PaymentStatus.PENDING)
//                .paymentMode(request.getPaymentMode() != null ? PaymentMode.valueOf(request.getPaymentMode()) : null)
//                .billingDate(LocalDateTime.now())
//                .build();
//
//        billingMasterRepository.save(master);
//
//        // Create IPDBillingDetails with new fields
//        IPDBillingDetails details = new IPDBillingDetails();
//        details.setBillingMaster(master);
//        details.setAdmissionId(admissionId);
//        details.setAdvancePaid(advance);
//        details.setTotalCharges(0.0);
//        details.setTotalPayments(0.0);
//        details.setDueAmount(0.0 - advance); // dueAmount = totalCharges - (advance + payments)
//        details.setBillingStatus("ACTIVE");
//        details.setTotal(0.0);
//        details.setTotalBeforeDiscount(0.0);
//        details.setTotalAfterDiscountAndGst(0.0);
//
//        ipdBillingRepository.save(details);
//
//        return new CreateIpdBillingAccountResponse(
//                details.getId(),
//                admissionId,
//                advance,
//                0.0,
//                0.0 - advance,
//                "ACTIVE",
//                "Billing account created successfully. Advance: ₹" + advance
//        );
//    }
//    /* --------------------------------------------------------------
//       1. CORE BILLING (unchanged – copy-paste from your old file)
//       -------------------------------------------------------------- */
////    @Override
////    public IPDBillingDetails generateIpdBill(IpdBillRequestDTO request) {
////        long daysAdmitted = Math.max(1,
////                ChronoUnit.DAYS.between(request.getAdmissionDate(),
////                        request.getDischargeDate()) + 1);
////
////        double roomCharges          = request.getRoomRatePerDay() * daysAdmitted;
////        double medicationCharges    = request.getMedicationCharges();
////        double doctorFees           = request.getDoctorFee();
////        double nursingCharges       = request.getNursingCharges();
////        double diagnosticCharges    = request.getDiagnosticCharges();
////        double procedureCharges     = request.getProcedureCharges();
////        double foodCharges          = request.getFoodCharges();
////        double miscCharges          = request.getMiscellaneousCharges();
////
////        double totalBeforeDiscount = roomCharges + medicationCharges + doctorFees + nursingCharges +
////                diagnosticCharges + procedureCharges + foodCharges + miscCharges;
////
////        double discountAmount      = totalBeforeDiscount * (request.getDiscountPercentage() / 100);
////        double totalAfterDiscount  = totalBeforeDiscount - discountAmount;
////
////        double gstAmount           = totalAfterDiscount * (request.getGstPercentage() / 100);
////        double finalTotal          = totalAfterDiscount + gstAmount;
////
////        // ----- BillingMaster -----
////        BillingMaster billingMaster = new BillingMaster();
////        billingMaster.setHospitaExternallId(request.getHospitalExternalId());
////        billingMaster.setPatientExternalId(request.getPatientExternalId());
////        billingMaster.setAdmissionId(request.getAdmissionId());
////        billingMaster.setModuleType("IPD");
////        billingMaster.setPaymentStatus(PaymentStatus.PENDING);
////        billingMaster.setTotalAmount(finalTotal);
////        billingMasterRepository.save(billingMaster);
////
////        // ----- IPDBillingDetails -----
////        IPDBillingDetails details = new IPDBillingDetails();
////        details.setBillingMaster(billingMaster);
////        details.setAdmissionId(request.getAdmissionId());
////        details.setRoomCharges(roomCharges);
////        details.setMedicationCharges(medicationCharges);
////        details.setDoctorFees(doctorFees);
////        details.setNursingCharges(nursingCharges);
////        details.setDiagnosticCharges(diagnosticCharges);
////        details.setProcedureCharges(procedureCharges);
////        details.setFoodCharges(foodCharges);
////        details.setMiscellaneousCharges(miscCharges);
////        details.setDaysAdmitted(daysAdmitted);
////        details.setTotalBeforeDiscount(totalBeforeDiscount);
////        details.setDiscountPercentage(request.getDiscountPercentage());
////        details.setServiceCharges(0.0);
////        details.setDiscountAmount(discountAmount);
////        details.setTotalAfterDiscountAndGst(totalAfterDiscount);
////        details.setGstPercentage(request.getGstPercentage());
////        details.setGstAmount(gstAmount);
////        details.setTotal(finalTotal);
////
////        return ipdBillingRepository.save(details);
////    }
//    
//    @Override
//    @Transactional
//    public IPDBillingDetails generateIpdBill(IpdBillRequestDTO request) {
//        Long admissionId = request.getAdmissionId();
//
//        // Prevent duplicate bill generation
//        if (ipdBillingRepository.findByAdmissionId(admissionId).isPresent()) {
//            throw new IllegalStateException("Bill already generated for admission ID: " + admissionId);
//        }
//
//        long daysAdmitted = Math.max(1,
//                ChronoUnit.DAYS.between(request.getAdmissionDate(), request.getDischargeDate()) + 1);
//
//        // === Calculate all charges ===
//        double roomCharges = request.getRoomRatePerDay() * daysAdmitted;
//        double medicationCharges = request.getMedicationCharges() != null ? request.getMedicationCharges() : 0.0;
//        double doctorFees = request.getDoctorFee() != null ? request.getDoctorFee() : 0.0;
//        double nursingCharges = request.getNursingCharges() != null ? request.getNursingCharges() : 0.0;
//        double diagnosticCharges = request.getDiagnosticCharges() != null ? request.getDiagnosticCharges() : 0.0;
////        double procedureCharges = request.getProcedureCharges() != null ? request.getProcedureCharges() : 0.0;
//        double foodCharges = request.getFoodCharges() != null ? request.getFoodCharges() : 0.0;
//        double miscCharges = request.getMiscellaneousCharges() != null ? request.getMiscellaneousCharges() : 0.0;
//
//        double totalBeforeDiscount = roomCharges + medicationCharges + doctorFees + nursingCharges +
//                diagnosticCharges  + foodCharges + miscCharges;
//
//        double discountAmount = totalBeforeDiscount * (request.getDiscountPercentage() != null ? request.getDiscountPercentage() : 0.0) / 100.0;
//        double totalAfterDiscount = totalBeforeDiscount - discountAmount;
//        double gstAmount = totalAfterDiscount * (request.getGstPercentage() != null ? request.getGstPercentage() : 0.0) / 100.0;
//        double finalTotal = totalAfterDiscount + gstAmount;
//
//        // === Advance Amount Handling ===
//        Double advancePaid = (request.getAdvanceAmount() != null && request.getAdvanceAmount() > 0)
//                ? request.getAdvanceAmount() : 0.0;
//
//        // === Create BillingMaster ===
//        BillingMaster billingMaster = BillingMaster.builder()
//                .hospitaExternallId(request.getHospitalExternalId())
//                .patientExternalId(request.getPatientExternalId())
//                .admissionId(admissionId)
//                .moduleType("IPD")
//                .totalAmount(finalTotal)
//                .paymentStatus(PaymentStatus.PENDING)
//                .advancePaymentMode(request.getAdvancePaymentMode())
//                .billingDate(LocalDateTime.now())
//                .build();
//        billingMasterRepository.save(billingMaster);
//
//        // === Create IPDBillingDetails with NEW FLOW ===
//        IPDBillingDetails details = new IPDBillingDetails();
//        details.setBillingMaster(billingMaster);
//        details.setAdmissionId(admissionId);
//
//        // Old breakdown fields
//        details.setRoomCharges(roomCharges);
//        details.setMedicationCharges(medicationCharges);
//        details.setDoctorFees(doctorFees);
//        details.setNursingCharges(nursingCharges);
//        details.setDiagnosticCharges(diagnosticCharges);
////        details.setProcedureCharges(procedureCharges);
//        details.setFoodCharges(foodCharges);
//        details.setMiscellaneousCharges(miscCharges);
//        details.setDaysAdmitted(daysAdmitted);
//
//        // Discount & GST
//        details.setDiscountPercentage(request.getDiscountPercentage() != null ? request.getDiscountPercentage() : 0.0);
//        details.setDiscountAmount(discountAmount);
//        details.setGstPercentage(request.getGstPercentage() != null ? request.getGstPercentage() : 0.0);
//        details.setGstAmount(gstAmount);
//        details.setTotalBeforeDiscount(totalBeforeDiscount);
//        details.setTotalAfterDiscountAndGst(totalAfterDiscount);
//        details.setTotal(finalTotal);
//        details.setServiceCharges(0.0);
//
//        // === NEW FIELDS - CORE OF THE NEW FLOW ===
//        details.setAdvancePaid(advancePaid);
//        details.setTotalCharges(finalTotal);           // At generation time: totalCharges = finalTotal
//        details.setTotalPayments(0.0);                 // No payments yet
//        details.setDueAmount(finalTotal - advancePaid); // Key formula
//        details.setBillingStatus("ACTIVE");            // Bill is now live!
//
//        return ipdBillingRepository.save(details);
//    }
//
//
//    @Override
//    public String processPayment(IpdPaymentRequestDTO request) {
//        BillingMaster billingMaster = billingMasterRepository.findByAdmissionId(request.getAdmissionId())
//                .orElseThrow(() -> new RuntimeException("Billing not found!"));
//
//        billingMaster.setPaymentStatus(PaymentStatus.PAID);
//        billingMaster.setPaymentMode(request.getPaymentMode());
//        billingMasterRepository.save(billingMaster);
//
//        return "Payment processed successfully for Admission ID: " + request.getAdmissionId();
//    }
//    
////    New service for Partial payment
//    @Override
//    @Transactional
//    public void makePartialPayment(IpdPartialPaymentRequestDTO request) {
//        IPDBillingDetails billing = ipdBillingRepository.findByAdmissionId(request.getAdmissionId())
//            .orElseThrow(() -> new RuntimeException("Billing not found for admission ID: " + request.getAdmissionId()));
//
//        // Only block if patient already discharged
//        if ("CLOSED".equals(billing.getBillingStatus())) {
//            throw new IllegalStateException("Patient is already discharged. Cannot accept payment.");
//        }
//
//        double amount = request.getAmount();
//        if (amount <= 0) {
//            throw new IllegalArgumentException("Payment amount must be greater than zero");
//        }
//
//        double currentDue = billing.getDueAmount();
//        if (amount > currentDue && currentDue > 0.01) {
//            throw new IllegalArgumentException(
//                "Amount exceeds current due of ₹" + String.format("%.2f", currentDue)
//            );
//        }
//        
//     // === RECORD PAYMENT IN HISTORY ===
//        IpdPaymentHistory history = IpdPaymentHistory.builder()
//            .admissionId(request.getAdmissionId())
//            .amount(amount)
//            .paymentMode(request.getPaymentMode())
//            .paymentDate(LocalDateTime.now())
//            .paidBy("Counter Staff") // Can be enhanced
//            .receiptNo("REC" + System.currentTimeMillis())
//            .billingDetails(billing)
//            .createdAt(LocalDateTime.now())   // THIS LINE WAS MISSING!
//            .build();
//        
//     // Add to billing's history list
//        if (billing.getPaymentHistory() == null) {
//            billing.setPaymentHistory(new ArrayList<>());
//        }
//        billing.getPaymentHistory().add(history);
//
//        // Increase totalPayments
//        double newTotalPayments = (billing.getTotalPayments() != null ? billing.getTotalPayments() : 0.0) + amount;
//        billing.setTotalPayments(newTotalPayments);
//
//        // Recalculate due amount (allow negative = credit)
//        double newDue = billing.getTotalCharges() - (billing.getAdvancePaid() + newTotalPayments);
//        billing.setDueAmount(newDue);
//
//        // DO NOT CLOSE BILL HERE
//        // DO NOT SET PAID STATUS
//        // Bill stays ACTIVE until discharge
//
//        // Optional: Update last payment mode in master (for receipt)
//        BillingMaster master = billing.getBillingMaster();
//        if (master != null) {
//            master.setPaymentMode(PaymentMode.valueOf(request.getPaymentMode()));
//            billingMasterRepository.save(master);
//        }
//
//        // Always recalculate (in case of floating point drift)
//        billing.recalculateDueAmount();
//
//        ipdPaymentHistoryRepository.save(history);
//        ipdBillingRepository.save(billing);
//    }
//
//    @Override
//    public IpdBillingDetailsResponse getBillingDetailsByAdmissionId(Long admissionId) {
//        IPDBillingDetails details = ipdBillingRepository.findByAdmissionId(admissionId)
//                .orElseThrow(() -> new RuntimeException("No billing found for admission ID: " + admissionId));
//        
//        List<IPDServiceUsage> serviceList=  ipdServiceUsageRepository.findByIpdBillingDetailsId(details.getId());
//        
//        IpdBillingDetailsResponse response = new IpdBillingDetailsResponse();
//        response.setId(details.getId());
//        response.setAdmissionId(details.getAdmissionId());
//        response.setRoomCharges(details.getRoomCharges());
//        response.setMedicationCharges(details.getMedicationCharges());
//        response.setDoctorFees(details.getDoctorFees());
//        response.setNursingCharges(details.getNursingCharges());
//        response.setDiagnosticCharges(details.getDiagnosticCharges());
//        response.setProcedureCharges(details.getProcedureCharges());
//        response.setFoodCharges(details.getFoodCharges());
//        response.setMiscellaneousCharges(details.getMiscellaneousCharges());
//        response.setDaysAdmitted(details.getDaysAdmitted());
//        response.setTotal(details.getTotal());
//        response.setIpdServices(serviceList);
//        response.setDiscountAmount(details.getDiscountAmount());
//        response.setDiscountPercentage(details.getDiscountPercentage());
//        response.setGstAmount(details.getGstAmount());
//        response.setGstPercentage(details.getGstPercentage());
//        response.setTotalBeforeDiscount(details.getTotalBeforeDiscount());
//        response.setTotalAfterDiscountAndGst(details.getTotalAfterDiscountAndGst());
//        response.setAdvanceAmount(details.getAdvancePaid());
//        response.setDueAmmount(details.getDueAmount());
//        response.setTotalPayedAmmount(details.getTotalPayments());
//        response.setBillingStatus(details.getBillingStatus());
//
//        IpdBillingDetailsResponse.BillingMasterDTO bm = new IpdBillingDetailsResponse.BillingMasterDTO();
//        bm.setId(details.getBillingMaster().getId());
//        bm.setHospitaExternallId(details.getBillingMaster().getHospitaExternallId());
//        bm.setPatientExternalId(details.getBillingMaster().getPatientExternalId());
//        bm.setAdmissionId(details.getBillingMaster().getAdmissionId());
//        bm.setModuleType(details.getBillingMaster().getModuleType());
//        bm.setTotalAmount(details.getBillingMaster().getTotalAmount());
//        bm.setPaymentStatus(details.getBillingMaster().getPaymentStatus().name());
//        bm.setPaymentMode(details.getBillingMaster().getPaymentMode() != null
//                ? details.getBillingMaster().getPaymentMode().name() : null);
//        bm.setAdvancePaymentMode(details.getBillingMaster().getAdvancePaymentMode());
//        bm.setBillingDate(details.getBillingMaster().getBillingDate());
//        response.setBillingMaster(bm);
//
//        return response;
//    }
//    
//    
////    Update the billing
////    @Override
////    @Transactional
////    public IPDBillingDetails updateIpdBill(IpdBillUpdateRequestDTO request) {
////        IPDBillingDetails billing = ipdBillingRepository.findByAdmissionId(request.getAdmissionId())
////                .orElseThrow(() -> new RuntimeException("No billing found for admission ID: " + request.getAdmissionId()));
////
////        long daysAdmitted = Math.max(1, ChronoUnit.DAYS.between(request.getAdmissionDate(), request.getDischargeDate()) + 1);
////
////        // === RECALCULATE ALL DAILY CHARGES FRESH ===
////        double roomCharges = request.getRoomRatePerDay() * daysAdmitted;
////
////        double nursingCharges = request.getNursingChargesPerDay() * daysAdmitted;
////        double foodCharges = request.getFoodChargesPerDay() * daysAdmitted;
////        double diagnosticCharges = request.getDiagnosticChargesPerDay() * daysAdmitted;
////        double miscDailyCharges = request.getMiscChargesPerDay() * daysAdmitted;
////
////        // === USE LATEST ACCUMULATED VALUES FROM REQUEST (NOT OLD DB VALUES!) ===
////        double medicationCharges = request.getMedicationCharges();
////        double doctorFees = request.getDoctorFee();                    // Now correct × days
////        double procedureCharges = request.getProcedureCharges();
////        double extraServiceCharges = request.getExtraServiceCharges(); // nursing + food + diag + misc extras
////
////        double totalMisc = miscDailyCharges + extraServiceCharges;
////        
////     // === IMPORTANT: ADD EXISTING SERVICE CHARGES ===
////        double existingServiceCharges =
////                billing.getServiceCharges() != null ? billing.getServiceCharges() : 0.0;
////
////        double totalBeforeDiscount = roomCharges +
////                medicationCharges +
////                doctorFees +
////                nursingCharges +
////                diagnosticCharges +
////                procedureCharges +
////                foodCharges +
////                totalMisc +
////                existingServiceCharges;;
////
////        double discountAmount = totalBeforeDiscount * (request.getDiscountPercentage() / 100.0);
////        double totalAfterDiscount = totalBeforeDiscount - discountAmount;
////        double gstAmount = totalAfterDiscount * (request.getGstPercentage() / 100.0);
////        double finalTotal = totalAfterDiscount + gstAmount;
////
////        // === UPDATE ALL FIELDS WITH FRESH VALUES ===
////        billing.setDaysAdmitted(daysAdmitted);
////        billing.setRoomCharges(roomCharges);
////        billing.setMedicationCharges(medicationCharges);
////        billing.setDoctorFees(doctorFees);
////        billing.setNursingCharges(nursingCharges);
////        billing.setDiagnosticCharges(diagnosticCharges);
////        billing.setProcedureCharges(procedureCharges);
////        billing.setFoodCharges(foodCharges);
////        billing.setMiscellaneousCharges(totalMisc);
////
////        billing.setTotalBeforeDiscount(totalBeforeDiscount);
////        billing.setDiscountAmount(discountAmount);
////        billing.setTotalAfterDiscountAndGst(totalAfterDiscount);
////        billing.setGstAmount(gstAmount);
////        billing.setTotal(finalTotal);
////        billing.setDiscountPercentage(request.getDiscountPercentage());
////        billing.setGstPercentage(request.getGstPercentage());
////
////        // Update master total
////        BillingMaster master = billing.getBillingMaster();
////        if (master != null) {
////            master.setTotalAmount(finalTotal);
////            billingMasterRepository.save(master);
////        }
////
////        return ipdBillingRepository.save(billing);
////    }
//
//    
//    @Override
//    @Transactional
//    public IPDBillingDetails updateIpdBill(IpdBillUpdateRequestDTO request) {
//        IPDBillingDetails billing = ipdBillingRepository.findByAdmissionId(request.getAdmissionId())
//                .orElseThrow(() -> new RuntimeException("No billing found for admission ID: " + request.getAdmissionId()));
//
//        // Prevent update if bill is already CLOSED
//        if ("CLOSED".equals(billing.getBillingStatus())) {
//            throw new IllegalStateException("Cannot update a CLOSED bill for admission ID: " + request.getAdmissionId());
//        }
//
//        long daysAdmitted = Math.max(1, ChronoUnit.DAYS.between(request.getAdmissionDate(), request.getDischargeDate()) + 1);
//
//        // === DAILY CHARGES (Room, Nursing, etc.) ===
//        double roomCharges = request.getRoomRatePerDay() * daysAdmitted;
//        double nursingCharges = request.getNursingChargesPerDay() * daysAdmitted;
//        double foodCharges = request.getFoodChargesPerDay() * daysAdmitted;
//        double diagnosticCharges = request.getDiagnosticChargesPerDay() * daysAdmitted;
////        double miscDailyCharges = request.getMiscChargesPerDay() * daysAdmitted;
//
//        // === ONE-TIME / ACCUMULATED CHARGES (from request) ===
//        double medicationCharges = request.getMedicationCharges() != null ? request.getMedicationCharges() : 0.0;
//        double doctorFees = request.getDoctorFee() != null ? request.getDoctorFee() : 0.0;
//        double procedureCharges = request.getProcedureCharges() != null ? request.getProcedureCharges() : 0.0;
//        double extraServiceCharges = request.getExtraServiceCharges() != null ? request.getExtraServiceCharges() : 0.0;
//
////        double totalMisc = miscDailyCharges + extraServiceCharges;
//
//     // Use the pre-calculated total sent from IPD service
//        double totalMisc = request.getMiscellaneousCharges() != null 
//            ? request.getMiscellaneousCharges() : 0.0;
//        
//        // === PRESERVE EXISTING SERVICE CHARGES (lab tests, consumables, etc.) ===
//        double existingServiceCharges = billing.getServiceCharges() != null ? billing.getServiceCharges() : 0.0;
//
//        // === NEW TOTAL BEFORE DISCOUNT (sum of everything) ===
//        double totalBeforeDiscount = roomCharges +
//                medicationCharges +
//                doctorFees +
//                nursingCharges +
//                diagnosticCharges +
//                procedureCharges +
//                foodCharges +
//                totalMisc +
//                existingServiceCharges;
//
//        double discountPercentage = request.getDiscountPercentage() != null ? request.getDiscountPercentage() : 0.0;
//        double discountAmount = totalBeforeDiscount * (discountPercentage / 100.0);
//        double totalAfterDiscount = totalBeforeDiscount - discountAmount;
//        double gstPercentage = request.getGstPercentage() != null ? request.getGstPercentage() : 0.0;
//        double gstAmount = totalAfterDiscount * (gstPercentage / 100.0);
//        double finalTotal = totalAfterDiscount + gstAmount;
//
//        // === PRESERVE ADVANCE & PAYMENTS (DO NOT TOUCH!) ===
//        double advancePaid = billing.getAdvancePaid() != null ? billing.getAdvancePaid() : 0.0;
//        double totalPayments = billing.getTotalPayments() != null ? billing.getTotalPayments() : 0.0;
//
//        // === UPDATE totalCharges = new finalTotal (this is cumulative now) ===
//        double newTotalCharges = finalTotal;
//
//        // === RECALCULATE DUE AMOUNT ===
//        double newDueAmount = newTotalCharges - (advancePaid + totalPayments);
//
//        // === UPDATE ALL FIELDS ===
//        billing.setDaysAdmitted(daysAdmitted);
//
//        billing.setRoomCharges(roomCharges);
//        billing.setMedicationCharges(medicationCharges);
//        billing.setDoctorFees(doctorFees);
//        billing.setNursingCharges(nursingCharges);
//        billing.setDiagnosticCharges(diagnosticCharges);
//        billing.setProcedureCharges(procedureCharges);
//        billing.setFoodCharges(foodCharges);
//        billing.setMiscellaneousCharges(totalMisc);
//
//        billing.setTotalBeforeDiscount(totalBeforeDiscount);
//        billing.setDiscountAmount(discountAmount);
//        billing.setTotalAfterDiscountAndGst(totalAfterDiscount);
//        billing.setGstAmount(gstAmount);
//        billing.setTotal(finalTotal);
//
//        billing.setDiscountPercentage(discountPercentage);
//        billing.setGstPercentage(gstPercentage);
//
//        // === NEW FLOW FIELDS - CRITICAL ===
//        billing.setTotalCharges(newTotalCharges);
//        billing.setDueAmount(newDueAmount);
//        billing.setBillingStatus("ACTIVE"); // Always ACTIVE until discharge
//
//        // Update BillingMaster total
//        BillingMaster master = billing.getBillingMaster();
//        if (master != null) {
//            master.setTotalAmount(newTotalCharges);
//            billingMasterRepository.save(master);
//        }
//
//        return ipdBillingRepository.save(billing);
//    }
//    /* --------------------------------------------------------------
//       2. GRANULAR CHARGES (services / doctor visits / medicines)
//       -------------------------------------------------------------- */
//    @Override
//    @Transactional
//    public List<IPDServiceUsage> addServices(AddServicesRequest request) {
//        IPDBillingDetails billing = ipdBillingRepository.findById(request.getIpdBillingId())
//                .orElseThrow(() -> new RuntimeException("IPD Billing not found"));
//
//        List<IPDServiceUsage> usages = request.getServices().stream().map(item -> {
//            Double total = item.getPrice() * item.getQuantity();
//            return IPDServiceUsage.builder()
//                    .ipdBillingDetails(billing)
//                    .serviceName(item.getServiceName())
//                    .price(item.getPrice())
//                    .quantity(item.getQuantity())
//                    .totalAmount(total)
//                    .serviceAddDate(LocalDateTime.now())
//                    .build();
//        }).toList();
//
//        double newServiceTotal = request.getServices().stream()
//                .mapToDouble(item -> item.getPrice() * item.getQuantity())
//                .sum();
//
//        // Update service charges
//        double currentServiceCharges = billing.getServiceCharges() != null ? billing.getServiceCharges() : 0.0;
//        billing.setServiceCharges(currentServiceCharges + newServiceTotal);
//
//        // Re-calculate total
//        double oldTotalBeforeDiscount = billing.getTotalBeforeDiscount();
//        double newTotalBeforeDiscount = oldTotalBeforeDiscount + newServiceTotal;
//
//        double discountAmount = newTotalBeforeDiscount * (billing.getDiscountPercentage() / 100.0);
//        double totalAfterDiscount = newTotalBeforeDiscount - discountAmount;
//        double gstAmount = totalAfterDiscount * (billing.getGstPercentage() / 100.0);
//        double finalTotal = totalAfterDiscount + gstAmount;
//
//        billing.setTotalBeforeDiscount(newTotalBeforeDiscount);
//        billing.setDiscountAmount(discountAmount);
//        billing.setTotalAfterDiscountAndGst(totalAfterDiscount);
//        billing.setGstAmount(gstAmount);
//        billing.setTotal(finalTotal);
//
//        // Update BillingMaster
//        BillingMaster master = billing.getBillingMaster();
//        if (master != null) {
//            master.setTotalAmount(finalTotal);
//            billingMasterRepository.save(master);
//        }
//
//        ipdBillingRepository.save(billing);
//        return ipdServiceUsageRepository.saveAll(usages);
//    }
//
//    @Override
//    public List<IPDDoctorVisit> addDoctorVisits(AddDoctorVisitsRequest request) {
//        IPDBillingDetails billing = ipdBillingRepository.findById(request.getIpdBillingId())
//                .orElseThrow(() -> new RuntimeException("IPD Billing not found"));
//
//        List<IPDDoctorVisit> visits = request.getVisits().stream()
//                .map(v -> IPDDoctorVisit.builder()
//                        .ipdBillingDetails(billing)
//                        .visitDate(v.getVisitDate())
//                        .doctorName(v.getDoctorName())
//                        .consultationFee(v.getConsultationFee())
//                        .build())
//                .toList();
//
//        return ipdDoctorVisitRepository.saveAll(visits);
//    }
//
//    @Override
//    public List<IPDMedication> addMedications(AddMedicationsRequest request) {
//        IPDBillingDetails billing = ipdBillingRepository.findById(request.getIpdBillingId())
//                .orElseThrow(() -> new RuntimeException("IPD Billing not found"));
//
//        List<IPDMedication> meds = request.getMedications().stream().map(m -> {
//            Double total = m.getPricePerUnit() * m.getQuantity();
//            return IPDMedication.builder()
//                    .ipdBillingDetails(billing)
//                    .medicineName(m.getMedicineName())
//                    .quantity(m.getQuantity())
//                    .pricePerUnit(m.getPricePerUnit())
//                    .totalPrice(total)
//                    .dosage(m.getDosage())
//                    .build();
//        }).toList();
//
//        return ipdMedicationRepository.saveAll(meds);
//    }
//
//    @Override
//    public List<IPDServiceUsage> getServicesByBillingId(Long ipdBillingId) {
//        return ipdServiceUsageRepository.findByIpdBillingDetailsId(ipdBillingId);
//    }
//
//    @Override
//    public List<IPDDoctorVisit> getDoctorVisitsByBillingId(Long ipdBillingId) {
//        return ipdDoctorVisitRepository.findByIpdBillingDetailsId(ipdBillingId);
//    }
//
//    @Override
//    public List<IPDMedication> getMedicationsByBillingId(Long ipdBillingId) {
//        return ipdMedicationRepository.findByIpdBillingDetailsId(ipdBillingId);
//    }
//
//
////	@Override
////	public IPDBillingDetails updateIpdBIll(IpdBillRequestDTO request) {
////		// TODO Auto-generated method stub
////		return null;
////	}
//}



package com.billing.service.impl;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.billing.dto.*;
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

    @Override
    @Transactional
    public CreateIpdBillingAccountResponse createBillingAccount(CreateIpdBillingAccountRequest request) {
        Long admissionId = request.getAdmissionId();

        // Prevent duplicate billing account
        if (ipdBillingRepository.findByAdmissionId(admissionId).isPresent()) {
            throw new IllegalStateException("Billing account already exists for admission ID: " + admissionId);
        }

        if (billingMasterRepository.findByAdmissionId(admissionId).isPresent()) {
            throw new IllegalStateException("BillingMaster already exists for admission ID: " + admissionId);
        }

        Double advance = (request.getAdvanceAmount() != null && request.getAdvanceAmount() > 0)
                ? request.getAdvanceAmount() : 0.0;

        // Create BillingMaster
        BillingMaster master = BillingMaster.builder()
                .hospitaExternallId(request.getHospitalExternalId())
                .patientExternalId(request.getPatientExternalId())
                .admissionId(admissionId)
                .moduleType("IPD")
                .totalAmount(0.0)
                .paymentStatus(PaymentStatus.PENDING)
                .paymentMode(request.getPaymentMode() != null ? PaymentMode.valueOf(request.getPaymentMode()) : null)
                .billingDate(LocalDateTime.now())
                .build();

        billingMasterRepository.save(master);

        // Create IPDBillingDetails with new fields
        IPDBillingDetails details = new IPDBillingDetails();
        details.setBillingMaster(master);
        details.setAdmissionId(admissionId);
        details.setAdvancePaid(advance);
        details.setTotalCharges(0.0);
        details.setTotalPayments(0.0);
        details.setDueAmount(0.0 - advance); // dueAmount = totalCharges - (advance + payments)
        details.setBillingStatus("ACTIVE");
        details.setTotal(0.0);
        details.setTotalBeforeDiscount(0.0);
        details.setTotalAfterDiscountAndGst(0.0);

        ipdBillingRepository.save(details);

        return new CreateIpdBillingAccountResponse(
                details.getId(),
                admissionId,
                advance,
                0.0,
                0.0 - advance,
                "ACTIVE",
                "Billing account created successfully. Advance: ₹" + advance
        );
    }
    /* --------------------------------------------------------------
       1. CORE BILLING (unchanged – copy-paste from your old file)
       -------------------------------------------------------------- */
//    @Override
//    public IPDBillingDetails generateIpdBill(IpdBillRequestDTO request) {
//        long daysAdmitted = Math.max(1,
//                ChronoUnit.DAYS.between(request.getAdmissionDate(),
//                        request.getDischargeDate()) + 1);
//
//        double roomCharges          = request.getRoomRatePerDay() * daysAdmitted;
//        double medicationCharges    = request.getMedicationCharges();
//        double doctorFees           = request.getDoctorFee();
//        double nursingCharges       = request.getNursingCharges();
//        double diagnosticCharges    = request.getDiagnosticCharges();
//        double procedureCharges     = request.getProcedureCharges();
//        double foodCharges          = request.getFoodCharges();
//        double miscCharges          = request.getMiscellaneousCharges();
//
//        double totalBeforeDiscount = roomCharges + medicationCharges + doctorFees + nursingCharges +
//                diagnosticCharges + procedureCharges + foodCharges + miscCharges;
//
//        double discountAmount      = totalBeforeDiscount * (request.getDiscountPercentage() / 100);
//        double totalAfterDiscount  = totalBeforeDiscount - discountAmount;
//
//        double gstAmount           = totalAfterDiscount * (request.getGstPercentage() / 100);
//        double finalTotal          = totalAfterDiscount + gstAmount;
//
//        // ----- BillingMaster -----
//        BillingMaster billingMaster = new BillingMaster();
//        billingMaster.setHospitaExternallId(request.getHospitalExternalId());
//        billingMaster.setPatientExternalId(request.getPatientExternalId());
//        billingMaster.setAdmissionId(request.getAdmissionId());
//        billingMaster.setModuleType("IPD");
//        billingMaster.setPaymentStatus(PaymentStatus.PENDING);
//        billingMaster.setTotalAmount(finalTotal);
//        billingMasterRepository.save(billingMaster);
//
//        // ----- IPDBillingDetails -----
//        IPDBillingDetails details = new IPDBillingDetails();
//        details.setBillingMaster(billingMaster);
//        details.setAdmissionId(request.getAdmissionId());
//        details.setRoomCharges(roomCharges);
//        details.setMedicationCharges(medicationCharges);
//        details.setDoctorFees(doctorFees);
//        details.setNursingCharges(nursingCharges);
//        details.setDiagnosticCharges(diagnosticCharges);
//        details.setProcedureCharges(procedureCharges);
//        details.setFoodCharges(foodCharges);
//        details.setMiscellaneousCharges(miscCharges);
//        details.setDaysAdmitted(daysAdmitted);
//        details.setTotalBeforeDiscount(totalBeforeDiscount);
//        details.setDiscountPercentage(request.getDiscountPercentage());
//        details.setServiceCharges(0.0);
//        details.setDiscountAmount(discountAmount);
//        details.setTotalAfterDiscountAndGst(totalAfterDiscount);
//        details.setGstPercentage(request.getGstPercentage());
//        details.setGstAmount(gstAmount);
//        details.setTotal(finalTotal);
//
//        return ipdBillingRepository.save(details);
//    }
    
    
//    -> Will generate rounded value upto 2 decimal places
//    private double round2(double value) {
//        return Math.round(value * 100.0) / 100.0;
//    }
    
    
    //->Round to nearest whole Rupee/Value
    private double round0(double value) {
    	return Math.round(value); // round to nearest whole rupee
    }

    
    @Override
    @Transactional
    public IPDBillingDetails generateIpdBill(IpdBillRequestDTO request) {
        Long admissionId = request.getAdmissionId();

        // Prevent duplicate bill generation
        if (ipdBillingRepository.findByAdmissionId(admissionId).isPresent()) {
            throw new IllegalStateException("Bill already generated for admission ID: " + admissionId);
        }

        long daysAdmitted = Math.max(1,
                ChronoUnit.DAYS.between(request.getAdmissionDate(), request.getDischargeDate()) + 1);


        // === Calculate all charges ===
        double roomCharges = request.getRoomRatePerDay() * daysAdmitted;
        double medicationCharges = request.getMedicationCharges() != null ? request.getMedicationCharges() : 0.0;
        double doctorFees = request.getDoctorFee() != null ? request.getDoctorFee() : 0.0;
        double nursingCharges = request.getNursingCharges() != null ? request.getNursingCharges() : 0.0;
        double diagnosticCharges = request.getDiagnosticCharges() != null ? request.getDiagnosticCharges() : 0.0;
//        double procedureCharges = request.getProcedureCharges() != null ? request.getProcedureCharges() : 0.0;
        double foodCharges = request.getFoodCharges() != null ? request.getFoodCharges() : 0.0;
        double miscCharges = request.getMiscellaneousCharges() != null ? request.getMiscellaneousCharges() : 0.0;

        double totalBeforeDiscount = roomCharges + medicationCharges + doctorFees + nursingCharges +
                diagnosticCharges  + foodCharges + miscCharges;

        double discountAmount = totalBeforeDiscount * (request.getDiscountPercentage() != null ? request.getDiscountPercentage() : 0.0) / 100.0;
        double totalAfterDiscount = totalBeforeDiscount - discountAmount;
        double gstAmount = totalAfterDiscount * (request.getGstPercentage() != null ? request.getGstPercentage() : 0.0) / 100.0;
//        double finalTotal = totalAfterDiscount + gstAmount;
        double finalTotal = round0(totalAfterDiscount + gstAmount);


        // === Advance Amount Handling ===
        Double advancePaid = (request.getAdvanceAmount() != null && request.getAdvanceAmount() > 0)
                ? request.getAdvanceAmount() : 0.0;

        // === Create BillingMaster ===
        BillingMaster billingMaster = BillingMaster.builder()
                .hospitaExternallId(request.getHospitalExternalId())
                .patientExternalId(request.getPatientExternalId())
                .admissionId(admissionId)
                .moduleType("IPD")
                .totalAmount(finalTotal)
                .paymentStatus(PaymentStatus.PENDING)
                .advancePaymentMode(request.getAdvancePaymentMode())
                .billingDate(LocalDateTime.now())
                .build();
        billingMasterRepository.save(billingMaster);

        // === Create IPDBillingDetails with NEW FLOW ===
        IPDBillingDetails details = new IPDBillingDetails();
        details.setBillingMaster(billingMaster);
        details.setAdmissionId(admissionId);

        // Old breakdown fields
        details.setRoomCharges(roomCharges);
        details.setMedicationCharges(medicationCharges);
        details.setDoctorFees(doctorFees);
        details.setNursingCharges(nursingCharges);
        details.setDiagnosticCharges(diagnosticCharges);
//        details.setProcedureCharges(procedureCharges);
        details.setFoodCharges(foodCharges);
        details.setMiscellaneousCharges(miscCharges);
        details.setDaysAdmitted(daysAdmitted);

        // Discount & GST
        details.setDiscountPercentage(request.getDiscountPercentage() != null ? request.getDiscountPercentage() : 0.0);
        details.setDiscountAmount(discountAmount);
        details.setGstPercentage(request.getGstPercentage() != null ? request.getGstPercentage() : 0.0);
        details.setGstAmount(gstAmount);
        details.setTotalBeforeDiscount(totalBeforeDiscount);
        details.setTotalAfterDiscountAndGst(finalTotal);
        details.setTotal(finalTotal);
        details.setServiceCharges(0.0);

        // === NEW FIELDS - CORE OF THE NEW FLOW ===
        details.setAdvancePaid(advancePaid);
        details.setTotalCharges(finalTotal);           // At generation time: totalCharges = finalTotal
        details.setTotalPayments(0.0);                 // No payments yet
//        details.setDueAmount(finalTotal - advancePaid); // Key formula
        details.setDueAmount(round0(finalTotal - advancePaid));  // Rounded due
        details.setBillingStatus("ACTIVE");            // Bill is now live!

        return ipdBillingRepository.save(details);
    }


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

    
//    New service for Partial payment
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

    @Override
//    @Transactional(readOnly = true)
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

        // === DAILY CHARGES (Room, Nursing, etc.) ===
        double roomCharges = request.getRoomRatePerDay() * daysAdmitted;
        double nursingCharges = request.getNursingChargesPerDay() * daysAdmitted;
        double foodCharges = request.getFoodChargesPerDay() * daysAdmitted;
        double diagnosticCharges = request.getDiagnosticChargesPerDay() * daysAdmitted;
//        double miscDailyCharges = request.getMiscChargesPerDay() * daysAdmitted;

        // === ONE-TIME / ACCUMULATED CHARGES (from request) ===
        double medicationCharges = request.getMedicationCharges() != null ? request.getMedicationCharges() : 0.0;
        double doctorFees = request.getDoctorFee() != null ? request.getDoctorFee() : 0.0;
        double procedureCharges = request.getProcedureCharges() != null ? request.getProcedureCharges() : 0.0;
        double extraServiceCharges = request.getExtraServiceCharges() != null ? request.getExtraServiceCharges() : 0.0;

//        double totalMisc = miscDailyCharges + extraServiceCharges;

     // Use the pre-calculated total sent from IPD service
        double totalMisc = request.getMiscellaneousCharges() != null 
            ? request.getMiscellaneousCharges() : 0.0;
        
        // === PRESERVE EXISTING SERVICE CHARGES (lab tests, consumables, etc.) ===
        double existingServiceCharges = billing.getServiceCharges() != null ? billing.getServiceCharges() : 0.0;

        // === NEW TOTAL BEFORE DISCOUNT (sum of everything) ===
        double totalBeforeDiscount = roomCharges +
                medicationCharges +
                doctorFees +
                nursingCharges +
                diagnosticCharges +
                procedureCharges +
                foodCharges +
                totalMisc +
                existingServiceCharges;

        double discountPercentage = request.getDiscountPercentage() != null ? request.getDiscountPercentage() : 0.0;
        double discountAmount = totalBeforeDiscount * (discountPercentage / 100.0);
        double totalAfterDiscount = totalBeforeDiscount - discountAmount;
        double gstPercentage = request.getGstPercentage() != null ? request.getGstPercentage() : 0.0;
        double gstAmount = totalAfterDiscount * (gstPercentage / 100.0);
        double finalTotal = totalAfterDiscount + gstAmount;

        // === PRESERVE ADVANCE & PAYMENTS (DO NOT TOUCH!) ===
        double advancePaid = billing.getAdvancePaid() != null ? billing.getAdvancePaid() : 0.0;
        double totalPayments = billing.getTotalPayments() != null ? billing.getTotalPayments() : 0.0;

        // === UPDATE totalCharges = new finalTotal (this is cumulative now) ===
        double newTotalCharges = finalTotal;

        // === RECALCULATE DUE AMOUNT ===
        double newDueAmount = newTotalCharges - (advancePaid + totalPayments);

        // === UPDATE ALL FIELDS ===
        billing.setDaysAdmitted(daysAdmitted);

        billing.setRoomCharges(roomCharges);
        billing.setMedicationCharges(medicationCharges);
        billing.setDoctorFees(doctorFees);
        billing.setNursingCharges(nursingCharges);
        billing.setDiagnosticCharges(diagnosticCharges);
        billing.setProcedureCharges(procedureCharges);
        billing.setFoodCharges(foodCharges);
        billing.setMiscellaneousCharges(totalMisc);

        billing.setTotalBeforeDiscount(round0(totalBeforeDiscount));
        billing.setDiscountAmount(discountAmount);
        billing.setTotalAfterDiscountAndGst(round0(finalTotal));
        billing.setGstAmount(gstAmount);
        billing.setTotal(round0(finalTotal));

        billing.setDiscountPercentage(discountPercentage);
        billing.setGstPercentage(gstPercentage);

        // === NEW FLOW FIELDS - CRITICAL ===
        billing.setTotalCharges(round0(newTotalCharges));
        billing.setDueAmount(round0(newDueAmount));
        billing.setBillingStatus("ACTIVE"); // Always ACTIVE until discharge

        // Update BillingMaster total
        BillingMaster master = billing.getBillingMaster();
        if (master != null) {
            master.setTotalAmount(newTotalCharges);
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
        billing.setTotalAfterDiscountAndGst(finalTotal);
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