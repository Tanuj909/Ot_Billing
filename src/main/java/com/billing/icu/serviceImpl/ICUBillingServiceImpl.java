package com.billing.icu.serviceImpl;

import com.billing.enums.PaymentMode;
import com.billing.enums.PaymentStatus;
import com.billing.exception.BillingException;
import com.billing.exception.ErrorCode;
import com.billing.icu.dto.ICUBillingRequest;
import com.billing.icu.dto.ICUBillingResponse;
import com.billing.icu.dto.PaymentHistoryResponse;
import com.billing.icu.entity.ICUBillingDetails;
import com.billing.icu.entity.ICUEquipmentBilling;
import com.billing.icu.entity.ICUMedicationBilling;
import com.billing.icu.entity.ICUPayment;
import com.billing.icu.repository.ICUBillingDetailsRepository;
import com.billing.icu.repository.ICUPaymentRepository;
import com.billing.icu.service.ICUBillingService;
import com.billing.model.BillingMaster;
import com.billing.ot.enums.OTPaymentStatus;
import com.billing.ot.enums.OTPaymentType;
import com.billing.repository.BillingMasterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ICUBillingServiceImpl implements ICUBillingService {

    private final ICUBillingDetailsRepository icuBillingRepo;
    private final BillingMasterRepository billingMasterRepo;
    private final ICUPaymentRepository icuPaymentRepository;

    // ──────────────────────────────────────────────────────────────
    // CREATE OR UPDATE BILL
    // ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ICUBillingResponse createOrUpdateBill(ICUBillingRequest request) {
        log.info("ICU bill create/update request for admissionId: {}", request.getIcuAdmissionId());

        ICUBillingDetails details = icuBillingRepo
                .findByIcuAdmissionId(request.getIcuAdmissionId())
                .orElse(null);

        if (details == null) {
            details = createFreshBill(request);
        } else {
            details = recalculateBill(details, request);
        }

        return mapToResponse(details);
    }

    // ──────────────────────────────────────────────────────────────
    // GET BILL
    // ──────────────────────────────────────────────────────────────

    @Override
    public ICUBillingResponse getBillByAdmission(Long icuAdmissionId) {
        ICUBillingDetails details = icuBillingRepo.findByIcuAdmissionId(icuAdmissionId)
                .orElseThrow(() -> new RuntimeException("Bill not found for admissionId: " + icuAdmissionId));
        return mapToResponse(details);
    }

    @Override
    public ICUBillingResponse getBillById(Long billingDetailsId) {
        ICUBillingDetails details = icuBillingRepo.findById(billingDetailsId)
                .orElseThrow(() -> new RuntimeException("ICU Billing not found: " + billingDetailsId));
        return mapToResponse(details);
    }

    // ──────────────────────────────────────────────────────────────
    // GET TOTAL AMOUNT
    // ──────────────────────────────────────────────────────────────

    @Override
    public Double getTotalAmount(Long admissionId) {
        ICUBillingDetails billingDetails = icuBillingRepo
                .findByIcuAdmissionId(admissionId)
                .orElseThrow(() ->
                        new BillingException(
                                "ICU bill not found for admission id: " + admissionId,
                                ErrorCode.BILL_NOT_FOUND,
                                HttpStatus.BAD_REQUEST
                        )
                );
        return billingDetails.getTotalAmount();
    }
    
    
    // ─────────────────────────────────────────────────────────────
    // GET PAYMENT HISTORY
    // ─────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<PaymentHistoryResponse> getPaymentHistory(Long admissionId) {

        log.info("Fetching ICU payment history for admissionId={}", admissionId);

        ICUBillingDetails billing = icuBillingRepo
                .findByIcuAdmissionId(admissionId)
                .orElseThrow(() ->
                        new BillingException(
                                "ICU bill not found for admissionId: " + admissionId,
                                ErrorCode.BILL_NOT_FOUND,
                                HttpStatus.NOT_FOUND
                        )
                );

        return icuPaymentRepository
                .findByIcuBillingDetailsIdOrderByPaidAtDesc(billing.getId())
                .stream()
                .map(this::mapToPaymentHistoryResponse)
                .toList();
    }

    // ──────────────────────────────────────────────────────────────
    // PAYMENT
    // ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ICUBillingResponse addPayment(Long icuAdmissionId, Double amount,
                                          PaymentMode paymentMode, String referenceNumber,
                                          String receivedBy) {

        ICUBillingDetails details = icuBillingRepo.findByIcuAdmissionId(icuAdmissionId)
                .orElseThrow(() -> new RuntimeException("Bill not found for admissionId: " + icuAdmissionId));

        double newAdvance = (details.getAdvancePaid() != null ? details.getAdvancePaid() : 0.0) + amount;
        OTPaymentType paymentType;
        if (newAdvance >= details.getTotalAmount()) {
            paymentType = OTPaymentType.FULL;
        } else if (newAdvance > 0) {
            paymentType = OTPaymentType.PARTIAL;
        } else {
            paymentType = OTPaymentType.ADVANCE;
        }

        ICUPayment payment = ICUPayment.builder()
                .icuBillingDetails(details)
                .patientExternalId(details.getPatientExternalId())
                .paymentType(paymentType)
                .paymentMode(paymentMode)
                .amount(amount)
                .referenceNumber(referenceNumber)
                .receivedBy(receivedBy)
                .status(OTPaymentStatus.SUCCESS)
                .build();

        details.getPayments().add(payment);
        details.setAdvancePaid(newAdvance);
        details.setDue(Math.max(0, details.getTotalAmount() - newAdvance));

        BillingMaster master = details.getBillingMaster();
        if (details.getDue() <= 0) {
            master.setPaymentStatus(PaymentStatus.PAID);
            details.setBillingStatus("CLOSED");
        } else {
            master.setPaymentStatus(PaymentStatus.PARTIAL);
        }
        billingMasterRepo.save(master);
        icuBillingRepo.save(details);

        log.info("Payment of {} recorded for admissionId: {}", amount, icuAdmissionId);
        return mapToResponse(details);
    }

    // ──────────────────────────────────────────────────────────────
    // CLOSE BILL
    // ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ICUBillingResponse closeBill(Long icuAdmissionId) {
        ICUBillingDetails details = icuBillingRepo.findByIcuAdmissionId(icuAdmissionId)
                .orElseThrow(() -> new RuntimeException("Bill not found for admissionId: " + icuAdmissionId));

        details.setBillingStatus("CLOSED");
        icuBillingRepo.save(details);

        BillingMaster master = details.getBillingMaster();
        master.setPaymentStatus(
                details.getDue() <= 0 ? PaymentStatus.PAID : PaymentStatus.PENDING
        );
        billingMasterRepo.save(master);

        return mapToResponse(details);
    }
    
 // ─────────────────────────────────────────────────────────────
    // 3. UPDATE BILLING STATUS
    // ─────────────────────────────────────────────────────────────
    @Override
    public ICUBillingResponse updateBillingStatus(
            Long admissionId,
            String status) {

        log.info("Updating ICU billing status | admissionId={} status={}",
                admissionId, status);

        ICUBillingDetails billing = icuBillingRepo
                .findByIcuAdmissionId(admissionId)
                .orElseThrow(() ->
                        new BillingException(
                                "ICU bill not found for admissionId: " + admissionId,
                                ErrorCode.BILL_NOT_FOUND,
                                HttpStatus.NOT_FOUND
                        )
                );

        // Optional validation
        List<String> allowedStatuses =
                List.of("ACTIVE", "CLOSED", "CANCELLED");

        if (!allowedStatuses.contains(status.toUpperCase())) {

            throw new BillingException(
                    "Invalid billing status: " + status,
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    HttpStatus.BAD_REQUEST
            );
        }

        billing.setBillingStatus(status.toUpperCase());

        ICUBillingDetails saved =
        		icuBillingRepo.save(billing);

        log.info("ICU billing status updated successfully | admissionId={} newStatus={}",
                admissionId, status);

        return ICUBillingResponse.builder()
                .billingId(saved.getId())
                .billingMasterId(
                        saved.getBillingMaster() != null
                                ? saved.getBillingMaster().getId()
                                : null
                )
                .icuAdmissionId(saved.getIcuAdmissionId())
                .hospitalId(saved.getHospitalExternalId())
                .patientId(saved.getPatientExternalId())
                .billingStatus(saved.getBillingStatus())
                .totalAmount(saved.getTotalAmount())
                .advancePaid(saved.getAdvancePaid())
                .due(saved.getDue())
                .build();
    }
    
    // ──────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ──────────────────────────────────────────────────────────────

    private ICUBillingDetails createFreshBill(ICUBillingRequest request) {

        BillingMaster master = BillingMaster.builder()
                .hospitaExternallId(request.getHospitalId())
                .patientExternalId(request.getPatientId())
                .icuAdmissionId(request.getIcuAdmissionId())
                .moduleType("ICU")
                .paymentStatus(PaymentStatus.PENDING)
                .totalAmount(0.0)
                .build();
        master = billingMasterRepo.save(master);

        // FIX: Builder se banate waqt empty lists initialize ho jaati hain (@Builder.Default)
        ICUBillingDetails details = ICUBillingDetails.builder()
                .billingMaster(master)
                .icuAdmissionId(request.getIcuAdmissionId())
                .hospitalExternalId(request.getHospitalId())
                .patientExternalId(request.getPatientId())
                .admittedAt(parseDateTime(request.getAdmittedAt()))
                .dischargedAt(parseDateTime(request.getDischargedAt()))
                .build();

        applyCalculation(details, request);

        master.setTotalAmount(details.getTotalAmount());
        billingMasterRepo.save(master);

        return icuBillingRepo.save(details);
    }

    private ICUBillingDetails recalculateBill(ICUBillingDetails details, ICUBillingRequest request) {
        log.debug("Recalculating ICU bill for admissionId: {}", request.getIcuAdmissionId());

        // FIX: clear() se Hibernate orphanRemoval DB se purane rows delete karega
        // Phir applyCalculation mein addAll() se naye rows add honge
        // KABHI bhi setEquipmentCharges(newList) mat karo — reference toot jaata hai
        details.getEquipmentCharges().clear();
        details.getMedicationCharges().clear();

        details.setAdmittedAt(parseDateTime(request.getAdmittedAt()));
        details.setDischargedAt(parseDateTime(request.getDischargedAt()));

        applyCalculation(details, request);

        BillingMaster master = details.getBillingMaster();
        master.setTotalAmount(details.getTotalAmount());
        billingMasterRepo.save(master);

        return icuBillingRepo.save(details);
    }

    /**
     * Core calculation — sab charges yahan compute hoti hain.
     * NOTE: Yeh method SIRF existing collection mein add karta hai (addAll).
     *       Kabhi bhi set***Charges(newList) mat karo — Hibernate crash hoga.
     */
    private void applyCalculation(ICUBillingDetails details, ICUBillingRequest request) {

        long days = request.getTotalDays() != null ? request.getTotalDays() : 1L;
        details.setTotalDays(days);

        // ── Daily charges ──────────────────────────────────────────
        double bedCharge        = safe(request.getBedChargePerDay())         * days;
        double nursingCharge    = safe(request.getNursingChargePerDay())     * days;
        double doctorCharge     = safe(request.getDoctorVisitChargePerDay()) * days;
        double monitoringCharge = safe(request.getMonitoringChargePerDay())  * days;
        double oxygenCharge     = safe(request.getOxygenChargePerDay())      * days;
        double admissionCharge  = safe(request.getAdmissionCharge());

        details.setBedCharge(bedCharge);
        details.setNursingCharge(nursingCharge);
        details.setDoctorCharge(doctorCharge);
        details.setMonitoringCharge(monitoringCharge);
        details.setOxygenCharge(oxygenCharge);
        details.setAdmissionCharge(admissionCharge);

        // ── Equipment charges ─────────────────────────────────────
        double totalEquipment = 0.0;

        if (request.getEquipmentUsages() != null) {
            for (ICUBillingRequest.EquipmentUsageItem item : request.getEquipmentUsages()) {

                ICUEquipmentBilling eq = ICUEquipmentBilling.builder()
                        .icuBillingDetails(details)
                        .equipmentAssignmentId(item.getEquipmentAssignmentId())
                        .equipmentExternalId(item.getEquipmentId())
                        .equipmentName(item.getEquipmentName())
                        .totalHours(item.getTotalHours())
                        .costPerHour(item.getCostPerHour())
                        .status(item.getStatus())
                        .build();

                if (item.getTotalCost() != null && item.getTotalCost() > 0) {
                    // ICU ne pre-calculated cost bheja — directly use karo
                    // FIX: totalHours aur costPerHour explicitly set karo — response mein null na aaye
//                    eq.setTotalHours(round2(item.getTotalHours()));
                	eq.setTotalHours(item.getTotalHours());
                    eq.setCostPerHour(item.getCostPerHour());
                    eq.setDiscountAmount(0.0);
                    eq.setDiscountPercent(0.0);
                    eq.setPriceAfterDiscount(round2(item.getTotalCost()));
                    eq.setGstAmount(0.0);
                    eq.setGstPercent(0.0);
                    eq.setTotalAmount(round2(item.getTotalCost()));
                } else {
                    eq.calculateAmounts();
                    // FIX: calculateAmounts ke baad round off karo
                    if (eq.getTotalAmount() != null) eq.setTotalAmount(round2(eq.getTotalAmount()));
                    if (eq.getDiscountAmount() != null) eq.setDiscountAmount(round2(eq.getDiscountAmount()));
                    if (eq.getPriceAfterDiscount() != null) eq.setPriceAfterDiscount(round2(eq.getPriceAfterDiscount()));
                    if (eq.getGstAmount() != null) eq.setGstAmount(round2(eq.getGstAmount()));
                }

                totalEquipment += safe(eq.getTotalAmount());

                // FIX: setEquipmentCharges() NAHI — addAll/add karo existing collection mein
                details.getEquipmentCharges().add(eq);
            }
        }
        details.setTotalEquipmentCharge(totalEquipment);

        // ── Medication charges ────────────────────────────────────
        double totalMedication = 0.0;

        if (request.getMedicationUsages() != null) {
            for (ICUBillingRequest.MedicationUsageItem item : request.getMedicationUsages()) {

                ICUMedicationBilling med = ICUMedicationBilling.builder()
                        .icuBillingDetails(details)
                        .medicationExternalId(item.getMedicationId())
                        .medicineName(item.getMedicineName())
                        .durationDays(item.getDurationDays())
                        .dosesPerDay(item.getDosesPerDay())
                        .costPerDose(item.getCostPerDose())
                        .build();

                if (item.getTotalCost() != null && item.getTotalCost() > 0) {
                    // Pre-calculated
                    med.setDiscountAmount(0.0);
                    med.setDiscountPercent(0.0);
                    med.setPriceAfterDiscount(item.getTotalCost());
                    med.setGstAmount(0.0);
                    med.setGstPercent(0.0);
                    med.setTotalAmount(item.getTotalCost());
                } else {
                    med.calculateAmounts();
                    if (med.getTotalAmount() != null) med.setTotalAmount(round2(med.getTotalAmount()));
                    if (med.getDiscountAmount() != null) med.setDiscountAmount(round2(med.getDiscountAmount()));
                    if (med.getPriceAfterDiscount() != null) med.setPriceAfterDiscount(round2(med.getPriceAfterDiscount()));
                    if (med.getGstAmount() != null) med.setGstAmount(round2(med.getGstAmount()));
                }

                totalMedication += safe(med.getTotalAmount());

                // FIX: setMedicationCharges() NAHI — add karo existing collection mein
                details.getMedicationCharges().add(med);
            }
        }
        details.setTotalMedicationCharge(totalMedication);

        // ── Summary ───────────────────────────────────────────────
        double gross = bedCharge + nursingCharge + doctorCharge + monitoringCharge
                + oxygenCharge + admissionCharge + totalEquipment + totalMedication;

        details.setGrossAmount(round2(gross));
        details.setTotalDiscountAmount(0.0);
        details.setTotalGstAmount(0.0);
        details.setTotalAmount(round2(gross));
        details.setDue(round2(Math.max(0, round2(gross) - safe(details.getAdvancePaid()))));
    }

    // ──────────────────────────────────────────────────────────────
    // MAPPING
    // ──────────────────────────────────────────────────────────────

    private ICUBillingResponse mapToResponse(ICUBillingDetails d) {

        List<ICUBillingResponse.EquipmentLineItem> eqItems =
                d.getEquipmentCharges() == null ? new ArrayList<>() :
                d.getEquipmentCharges().stream()
                        .map(eq -> ICUBillingResponse.EquipmentLineItem.builder()
                                .equipmentAssignmentId(eq.getEquipmentAssignmentId())
                                .equipmentExternalId(eq.getEquipmentExternalId())
                                .equipmentName(eq.getEquipmentName())
                                .totalHours(eq.getTotalHours())
                                .costPerHour(eq.getCostPerHour())
                                .discountPercent(eq.getDiscountPercent())
                                .discountAmount(eq.getDiscountAmount())
                                .gstPercent(eq.getGstPercent())
                                .gstAmount(eq.getGstAmount())
                                .totalAmount(eq.getTotalAmount())
                                .status(eq.getStatus())
                                .build())
                        .collect(Collectors.toList());

        List<ICUBillingResponse.MedicationLineItem> medItems =
                d.getMedicationCharges() == null ? new ArrayList<>() :
                d.getMedicationCharges().stream()
                        .map(med -> ICUBillingResponse.MedicationLineItem.builder()
                                .medicationExternalId(med.getMedicationExternalId())
                                .medicineName(med.getMedicineName())
                                .durationDays(med.getDurationDays())
                                .dosesPerDay(med.getDosesPerDay())
                                .costPerDose(med.getCostPerDose())
                                .discountPercent(med.getDiscountPercent())
                                .discountAmount(med.getDiscountAmount())
                                .gstPercent(med.getGstPercent())
                                .gstAmount(med.getGstAmount())
                                .totalAmount(med.getTotalAmount())
                                .build())
                        .collect(Collectors.toList());

        return ICUBillingResponse.builder()
                .billingId(d.getId())
                .billingMasterId(d.getBillingMaster() != null ? d.getBillingMaster().getId() : null)
                .icuAdmissionId(d.getIcuAdmissionId())
                .hospitalId(d.getHospitalExternalId())
                .patientId(d.getPatientExternalId())
                .totalDays(d.getTotalDays())
                .admittedAt(d.getAdmittedAt() != null ? d.getAdmittedAt().toString() : null)
                .dischargedAt(d.getDischargedAt() != null ? d.getDischargedAt().toString() : null)
                .bedCharge(d.getBedCharge())
                .nursingCharge(d.getNursingCharge())
                .doctorCharge(d.getDoctorCharge())
                .monitoringCharge(d.getMonitoringCharge())
                .oxygenCharge(d.getOxygenCharge())
                .admissionCharge(d.getAdmissionCharge())
                .totalEquipmentCharge(d.getTotalEquipmentCharge())
                .totalMedicationCharge(d.getTotalMedicationCharge())
                .grossAmount(d.getGrossAmount())
                .totalDiscountAmount(d.getTotalDiscountAmount())
                .totalGstAmount(d.getTotalGstAmount())
                .totalAmount(d.getTotalAmount())
                .advancePaid(d.getAdvancePaid())
                .due(d.getDue())
                .billingStatus(d.getBillingStatus())
                .equipmentCharges(eqItems)
                .medicationCharges(medItems)
                .build();
    }

    // ──────────────────────────────────────────────────────────────
    // UTILS
    // ──────────────────────────────────────────────────────────────

    private double safe(Double val) {
        return val != null ? val : 0.0;
    }

    /**
     * Round to 2 decimal places — DB aur response mein clean values aane ke liye.
     */
    private double round2(Double val) {
        if (val == null) return 0.0;
        return Math.round(val);
    }

    private LocalDateTime parseDateTime(String s) {
        if (s == null || s.isBlank()) return null;
        return LocalDateTime.parse(s, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
    
    // ─────────────────────────────────────────────────────────────
    // PRIVATE MAPPER
    // ─────────────────────────────────────────────────────────────
    private PaymentHistoryResponse mapToPaymentHistoryResponse(
            ICUPayment payment) {

        return PaymentHistoryResponse.builder()
                .paymentId(payment.getId())
                .billingMasterId(
                        payment.getIcuBillingDetails() != null
                                && payment.getIcuBillingDetails().getBillingMaster() != null
                                ? payment.getIcuBillingDetails().getBillingMaster().getId()
                                : null
                )
                .amount(payment.getAmount())
                .paymentMode(payment.getPaymentMode())
                .referenceNumber(payment.getReferenceNumber())
                .receivedBy(payment.getReceivedBy())
                .paidAt(payment.getPaidAt())
                .build();
    }
}