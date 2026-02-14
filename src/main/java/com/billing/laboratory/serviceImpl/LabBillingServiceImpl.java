package com.billing.laboratory.serviceImpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import com.billing.enums.PaymentStatus;
import com.billing.enums.RefundStatus;
import com.billing.laboratory.dto.*;
import com.billing.laboratory.entity.LabBillingDetails;
import com.billing.laboratory.entity.LabPayment;
import com.billing.laboratory.entity.LabRefund;
import com.billing.laboratory.entity.LabTestBilling;
import com.billing.laboratory.repository.LabBillingDetailsRepository;
import com.billing.laboratory.repository.LabPaymentRepository;
import com.billing.laboratory.repository.LabRefundRepository;
import com.billing.laboratory.repository.LabTestBillingRepository;
import com.billing.laboratory.service.LabBillingService;
import com.billing.model.BillingMaster;
import com.billing.repository.BillingMasterRepository;
import com.billing.util.AmountUtil;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class LabBillingServiceImpl implements LabBillingService {

    private final BillingMasterRepository billingMasterRepo;
    private final LabBillingDetailsRepository labBillingRepo;
    private final LabTestBillingRepository labTestBillingRepo;
    private final LabRefundRepository labRefundRepo;
    private final LabPaymentRepository labPaymentRepo;

    @Override
    @Transactional
    public GenerateLabBillResponse generateBill(GenerateLabBillRequest request) {

        double billTotal = 0.0;

        // 1. Create Billing Master (authoritative)
        BillingMaster billingMaster = BillingMaster.builder()
        		.hospitaExternallId(request.getHospitaExternallId())
                .labStoreId(request.getStoreId())
                .patientExternalId(request.getPatientExternalId())
                .billingDate(LocalDateTime.now())
                .labOrderId(request.getLabOrderId())
                .moduleType("LAB")
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        billingMasterRepo.save(billingMaster);

        // 2. Create Lab Billing Details
        LabBillingDetails labBilling = new LabBillingDetails();
        labBilling.setLabOrderId(request.getLabOrderId());
        labBilling.setBillingMaster(billingMaster);
        labBilling.setTotalPayment(0.0);
        labBilling.setBillingStatus("ACTIVE");

        labBillingRepo.save(labBilling);

        // 3. Process tests (GST CALCULATION HAPPENS HERE)
        for (LabTestBillItemDTO dto : request.getTests()) {

            double price = dto.getPrice();
            double gstPercentage =
                    dto.getGstPercentage() != null ? dto.getGstPercentage() : 0.0;

            // ✅ Calculate GST amount here
            double gstAmount = 0.0;
            if (gstPercentage > 0) {
                gstAmount = (price * gstPercentage) / 100;
            }

            gstAmount = AmountUtil.round(gstAmount);

            // ✅ Calculate test total
            double testTotal = AmountUtil.round(price + gstAmount);

            LabTestBilling testBilling = LabTestBilling.builder()
                    .labBillingDetails(labBilling)
                    .orderItemId(dto.getOrderItemId())
                    .testName(dto.getTestName())
                    .price(price)
                    .gstPercentage(gstPercentage)
                    .gstAmount(BigDecimal.valueOf(gstAmount))
                    .totalAmount(testTotal)
                    .build();

            labTestBillingRepo.save(testBilling);

            billTotal += testTotal;
        }

        billTotal = AmountUtil.round(billTotal);

        // 4. Finalize bill totals
        labBilling.setTestCharges(billTotal);
        labBilling.setDue(billTotal);

        billingMaster.setTotalAmount(billTotal);

        // 5. Response
        return GenerateLabBillResponse.builder()
                .billingId(billingMaster.getId())
                .labBillingId(labBilling.getId())
                .totalAmount(billTotal)
                .dueAmount(billTotal)
                .billingStatus(labBilling.getBillingStatus())
                .build();
    }

    
    @Transactional
    @Override
    public GenerateLabBillResponse updateBill(GenerateLabBillRequest request) {

        // 1️⃣ Fetch EXISTING BillingMaster (NO NEW CREATE)
        BillingMaster billingMaster =
                billingMasterRepo
                        .findByLabOrderIdAndPaymentStatus(
                                request.getLabOrderId(),
                                PaymentStatus.PENDING
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Active billing master not found for order"
                                )
                        );

        // 🚫 Safety: PAID bill cannot be modified
        if (billingMaster.getPaymentStatus() == PaymentStatus.PAID) {
            throw new IllegalStateException("Cannot update a PAID bill");
        }

        // 2️⃣ Fetch existing LabBillingDetails
        LabBillingDetails labBilling =
                labBillingRepo.findByBillingMaster(billingMaster)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Lab billing details not found"
                                )
                        );

        // 3️⃣ DELETE OLD TEST ROWS
        labTestBillingRepo.deleteByLabBillingDetails(labBilling);

        // 4️⃣ Recalculate tests
        double billTotal = 0.0;
        double totalGst = 0.0;

        for (LabTestBillItemDTO dto : request.getTests()) {

            double price = dto.getPrice();
            double gstPercentage =
                    dto.getGstPercentage() != null ? dto.getGstPercentage() : 0.0;

            double gstAmount = gstPercentage > 0
                    ? AmountUtil.round((price * gstPercentage) / 100)
                    : 0.0;

            double testTotal = AmountUtil.round(price + gstAmount);

            LabTestBilling testBilling = LabTestBilling.builder()
                    .labBillingDetails(labBilling)
                    .testName(dto.getTestName())
                    .price(price)
                    .gstPercentage(gstPercentage)
                    .gstAmount(BigDecimal.valueOf(gstAmount))
                    .totalAmount(testTotal)
                    .build();

            labTestBillingRepo.save(testBilling);

            billTotal += testTotal;
            totalGst += gstAmount;
        }

        billTotal = AmountUtil.round(billTotal);
        totalGst = AmountUtil.round(totalGst);

        // 5️⃣ UPDATE SAME LabBillingDetails
        labBilling.setTestCharges(billTotal);
        labBilling.setTestGstAmount(totalGst);
        labBilling.setDue(billTotal);
        labBilling.setUpdatedAt(LocalDateTime.now());

        // 6️⃣ UPDATE SAME BillingMaster
        billingMaster.setTotalAmount(billTotal);
        billingMaster.setUpdatedAt(LocalDateTime.now());

        // 7️⃣ RESPONSE
        return GenerateLabBillResponse.builder()
                .billingId(billingMaster.getId())
                .labBillingId(labBilling.getId())
                .totalAmount(billTotal)
                .dueAmount(billTotal)
                .billingStatus(labBilling.getBillingStatus())
                .build();
    }

    
    @Override
    @Transactional
    public void makePayment(LabPaymentRequest request) {
    	
        LabBillingDetails billing = labBillingRepo.findById(request.getLabBillingId())
                .orElseThrow(() -> new RuntimeException("Lab Billing not found"));

        if (request.getAmountPaid() == null || request.getAmountPaid() <= 0) {
            throw new RuntimeException("Payment amount must be greater than zero");
        }

        if (billing.getDue() == null || billing.getDue() <= 0) {
            throw new RuntimeException("No due amount pending for this bill");
        }

        if (request.getAmountPaid() > billing.getDue()) {
            throw new RuntimeException("Amount cannot be greater than due amount");
        }

        double totalAmount = AmountUtil.round(
                billing.getTestCharges()
                + (billing.getTestGstAmount() != null ? billing.getTestGstAmount() : 0)
                - (billing.getDiscountAmount() != null ? billing.getDiscountAmount() : 0)
        );

        double totalPaid = AmountUtil.round(
                (billing.getTotalPayment() != null ? billing.getTotalPayment() : 0)
                + request.getAmountPaid()
        );

        double due = AmountUtil.round(totalAmount - totalPaid);

        if (due < 0) {
            due = 0;
        }

        billing.setTotalPayment(totalPaid);
        billing.setDue(due);
        billing.setUpdatedAt(LocalDateTime.now());
        
//        INSERT INTO lab_payment HERE -> For Payment History
        LabPayment payment = LabPayment.builder()
                .labOrderId(billing.getLabOrderId())
                .billingId(billing.getBillingMaster().getId())
                .patientExternalId(
                        billing.getBillingMaster().getPatientExternalId()
                )
                .amount(request.getAmountPaid())
                .paymentMode(request.getPaymentMode())
                .paidAt(LocalDateTime.now())
//                .referenceNumber(request.getReferenceNumber())
                .build();

        labPaymentRepo.save(payment);

        if (totalPaid == 0) {
            billing.getBillingMaster().setPaymentStatus(PaymentStatus.PENDING);
        } else if (due == 0) {
            billing.getBillingMaster().setPaymentStatus(PaymentStatus.PAID);
        } else {
            billing.getBillingMaster().setPaymentStatus(PaymentStatus.PARTIAL);
        }

        labBillingRepo.save(billing);
    }

    
    @Override
    @Transactional
    public LabDiscountResponse applyDiscount(LabDiscountRequest request) {

        LabBillingDetails billing = labBillingRepo.findById(request.getLabBillingId())
                .orElseThrow(() -> new RuntimeException("Lab Billing not found"));

        double billAmount = billing.getTestCharges(); // ORIGINAL AMOUNT
        double discountAmount = 0.0;

        // 1. Calculate discount
        if (request.getDiscountPercentage() != null) {
            discountAmount = (billAmount * request.getDiscountPercentage()) / 100;
            billing.setDiscountPercentage(request.getDiscountPercentage());
        }

        if (request.getDiscountAmount() != null) {
            discountAmount = request.getDiscountAmount();
            billing.setDiscountPercentage(null);
        }

        discountAmount = AmountUtil.round(discountAmount);

        // 2. Persist discount
        billing.setDiscountAmount(discountAmount);

        // 3. Recalculate due ONLY
        double newDue = AmountUtil.round(
                billAmount - discountAmount - billing.getTotalPayment()
        );

        billing.setDue(Math.max(newDue, 0.0)); // prevent negative due

        // 4. Build response (DO NOT change test_charges)
        LabDiscountResponse response = new LabDiscountResponse();
        response.setTotalAmount(billAmount); // unchanged
        response.setDue(billing.getDue());
        response.setDiscountAmount(billing.getDiscountAmount());
        response.setDiscountPercentage(billing.getDiscountPercentage());
        response.setPaymentStatus(
                billing.getBillingMaster().getPaymentStatus().name()
        );

        return response;
    }


    
    @Override
    @Transactional
    public LabDiscountResponse removeDiscount(RemoveLabDiscountRequest request) {

        LabBillingDetails billing = labBillingRepo.findById(request.getLabBillingId())
                .orElseThrow(() -> new RuntimeException("Lab Billing not found"));

        // 1. Clear discount
        billing.setDiscountAmount(0.0);
        billing.setDiscountPercentage(null);

        // 2. Recalculate due ONLY (test_charges stays unchanged)
        double newDue = AmountUtil.round(
                billing.getTestCharges() - billing.getTotalPayment()
        );

        billing.setDue(Math.max(newDue, 0.0));

        // 3. Build response
        LabDiscountResponse response = new LabDiscountResponse();
        response.setTotalAmount(billing.getTestCharges()); // unchanged
        response.setDue(billing.getDue());
        response.setDiscountAmount(0.0);
        response.setDiscountPercentage(null);
        response.setPaymentStatus(
                billing.getBillingMaster().getPaymentStatus().name()
        );

        return response;
    }



    @Override
    public LabBillResponse getBillByLabOrder(Long labOrderId) {

        LabBillingDetails billing = labBillingRepo.findByLabOrderId(labOrderId)
                .orElseThrow(() ->
                        new RuntimeException("Bill not found for labOrderId: " + labOrderId));

        List<LabTestBillItemDTO> items =
                labTestBillingRepo.findByLabBillingDetailsId(billing.getId())
                        .stream()
                        .map(this::toDto)
                        .toList();

        return LabBillResponse.builder()
                .labBillingId(billing.getId())
                .billingId(billing.getBillingMaster().getId())
                .labOrderId(labOrderId)
                .totalAmount(billing.getTestCharges())
                .totalPaid(billing.getTotalPayment())
                .due(billing.getDue())
                .paymentStatus(billing.getBillingMaster().getPaymentStatus().name())
                .billingStatus(billing.getBillingStatus())
                .discountPercentage(billing.getDiscountPercentage())
                .discountAmount(billing.getDiscountAmount())
                .tests(items)
                .build();
    }
    
    private LabTestBillItemDTO toDto(LabTestBilling entity) {

        return LabTestBillItemDTO.builder()
                .id(entity.getId())
                .testName(entity.getTestName())
                .price(entity.getPrice())
                .gstPercentage(entity.getGstPercentage())
                .gstAmount(entity.getGstAmount())
                .totalAmount(entity.getTotalAmount())
//                .status(entity.getStatus())
                .build();
    }
    
    
    
    @Override
    public PaymentStatusResponse getPaymentStatus(Long labOrderId) {

        // 1️⃣ Fetch Billing Master
        BillingMaster billingMaster =
                billingMasterRepo.findByLabOrderId(labOrderId)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Billing not found for orderId: " + labOrderId
                                )
                        );

        // 2️⃣ Fetch Lab Billing Details
        LabBillingDetails labBilling =
                labBillingRepo.findByBillingMaster(billingMaster)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Lab billing details not found"
                                )
                        );

        // 3️⃣ Calculate Paid Amount
        double totalPaid =
                labBilling.getTotalPayment() != null
                        ? labBilling.getTotalPayment()
                        : 0.0;

        double due =
                labBilling.getDue() != null
                        ? labBilling.getDue()
                        : billingMaster.getTotalAmount();

        // 4️⃣ Decide Payment Status (safety)
        PaymentStatus status = billingMaster.getPaymentStatus();

        return PaymentStatusResponse.builder()
                .billingId(billingMaster.getId())
                .labOrderId(labOrderId)
                .paymentStatus(status)
                .totalAmount(billingMaster.getTotalAmount())
                .totalPaid(totalPaid)
                .dueAmount(due)
                .billingStatus(labBilling.getBillingStatus())
                .build();
    }
    
    
    @Override
    @Transactional
    public void cancelBilling(Long labOrderId) {

        // 1️⃣ Fetch Billing Master
        BillingMaster billingMaster =
                billingMasterRepo.findByLabOrderId(labOrderId)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Billing not found for orderId: " + labOrderId
                                )
                        );

        // 2️⃣ Block cancellation if PAID
        if (billingMaster.getPaymentStatus() == PaymentStatus.PAID) {
            throw new IllegalStateException(
                    "Paid billing cannot be cancelled"
            );
        }

        // 3️⃣ Fetch Lab Billing Details
        LabBillingDetails labBilling =
                labBillingRepo.findByBillingMaster(billingMaster)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Lab billing details not found"
                                )
                        );

        // 4️⃣ Cancel Billing (NO DELETE)
        billingMaster.setPaymentStatus(PaymentStatus.CANCELLED);
        billingMaster.setUpdatedAt(LocalDateTime.now());

        labBilling.setBillingStatus("CANCELLED");
        labBilling.setDue(0.0);
        labBilling.setUpdatedAt(LocalDateTime.now());

        // ❌ Do NOT delete LabTestBilling rows
        // ❌ Do NOT delete BillingMaster
    }

    
    @Override
    @Transactional
    public LabRefundResponse refund(LabRefundRequest request) {

        // 1️⃣ Fetch BillingMaster
        BillingMaster billingMaster =
                billingMasterRepo.findByLabOrderId(request.getLabOrderId())
                        .orElseThrow(() ->
                                new IllegalStateException("Billing not found")
                        );

        // 2️⃣ Refund only if PAID / PARTIALLY_PAID
        if (billingMaster.getPaymentStatus() == PaymentStatus.PENDING) {
            throw new IllegalStateException(
                    "Refund not allowed for unpaid bill"
            );
        }

        LabBillingDetails labBilling =
                labBillingRepo.findByBillingMaster(billingMaster)
                        .orElseThrow(() ->
                                new IllegalStateException("Lab billing not found")
                        );

        double paidAmount =
                labBilling.getTotalPayment() != null
                        ? labBilling.getTotalPayment()
                        : 0.0;

        if (request.getRefundAmount() > paidAmount) {
            throw new IllegalArgumentException(
                    "Refund amount cannot exceed paid amount"
            );
        }

        // 3️⃣ Create refund entry
        LabRefund refund = LabRefund.builder()
                .labOrderId(request.getLabOrderId())
                .billingId(billingMaster.getId())
                .labStoreId(billingMaster.getLabStoreId())
                .refundAmount(request.getRefundAmount())
                .reason(request.getReason())
                .refundStatus(RefundStatus.INITIATED)
                .build();

        labRefundRepo.save(refund);

        // 4️⃣ Update billing amounts
        double newPaid = paidAmount - request.getRefundAmount();
        labBilling.setTotalPayment(newPaid);
        labBilling.setDue(billingMaster.getTotalAmount() - newPaid);

        // 5️⃣ Update payment status
        if (newPaid == 0) {
            billingMaster.setPaymentStatus(PaymentStatus.PENDING);
        } else {
            billingMaster.setPaymentStatus(PaymentStatus.PARTIALLY_PAID);
        }

        // 6️⃣ Mark refund completed (simplified)
        refund.setRefundStatus(RefundStatus.COMPLETED);
        refund.setRefundedAt(LocalDateTime.now());

        return LabRefundResponse.builder()
                .refundId(refund.getId())
                .labOrderId(refund.getLabOrderId())
                .refundAmount(refund.getRefundAmount())
                .refundStatus(refund.getRefundStatus().name())
                .build();
    }
    
    @Override
    public RefundStatusResponse getRefundStatus(Long orderId) {

        // 1️⃣ Fetch refund record
        LabRefund refund =
                labRefundRepo.findByLabOrderId(orderId)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Refund not found Order ID: " + orderId
                                )
                        );

        // 2️⃣ Map response
        return RefundStatusResponse.builder()
                .refundId(refund.getId())
                .labOrderId(refund.getLabOrderId())
                .billingId(refund.getBillingId())
                .refundAmount(refund.getRefundAmount())
                .refundStatus(refund.getRefundStatus().name())
                .reason(refund.getReason())
                .createdAt(refund.getCreatedAt())
                .refundedAt(refund.getRefundedAt())
                .build();
    }
    
    
    @Override
    public RefundReportResponse getRefundReport(Long storeId) {
    	
    	List<LabRefund> refunds = labRefundRepo.getRefundReportByLabStoreId(storeId)
    			.orElseThrow(()-> new ResourceAccessException("Refund for this Store Not Available!"));
    	
    	double totalAmount = 
    			refunds.stream()
    			.mapToDouble(LabRefund::getRefundAmount)
    			.sum();
    	
    	List<RefundReportItemDTO> items =
    			refunds.stream()
    			.map(refund -> RefundReportItemDTO.builder()
    					.refundId(refund.getId())
    					.labOrderId(refund.getLabOrderId())
    					.refundAmount(refund.getRefundAmount())
    					.billingId(refund.getBillingId())
    					.reason(refund.getReason())
    					.refundStatus(refund.getRefundStatus().name())
    					.refundedAt(refund.getRefundedAt())
    					.build()
    				)
    			.toList();
    	
    	RefundReportResponse response = RefundReportResponse.builder()
    			.totalRefundAmount(totalAmount)
    			.totalRefundCount(items.size())
    			.refunds(items)
    			.build();
    			
    	return response;
    }
    
//    @Override
//    public String processLabTestRefund(Long labOrderId,
//                                       Long orderItemId,
//                                       String reason) {
//
//        // 1️⃣ Fetch Billing Master
//        BillingMaster billingMaster = billingMasterRepo
//                .findByLabOrderId(labOrderId)
//                .orElseThrow(() -> new ResourceAccessException("BillingMaster not found"));
//
//        // 2️⃣ Fetch Billing Details
//        LabBillingDetails billingDetails = labBillingRepo
//                .findByLabOrderIdAndBillingMaster_Id(
//                        labOrderId,
//                        billingMaster.getId())
//                .orElseThrow(() -> new ResourceAccessException("Billing details not found"));
//
//        // 3️⃣ Fetch Test Billing
//        LabTestBilling testBilling = labTestBillingRepo
//                .findByLabBillingDetails_IdAndOrderItemId(
//                        billingDetails.getId(),
//                        orderItemId)
//                .orElseThrow(() -> new ResourceAccessException("Test billing not found"));
//
//        // 4️⃣ Prevent Double Refund
//        boolean alreadyRefunded = labRefundRepo
//                .existsByBillingIdAndOrderItemIdAndRefundStatus(
//                        billingMaster.getId(),
//                        orderItemId,
//                        RefundStatus.PROCESSED);
//
//        if (alreadyRefunded) {
//            throw new IllegalStateException("This test is already refunded");
//        }
//
//        // 5️⃣ Financial Calculation (Discount Safe)
//
//        BigDecimal orderTotal =
//                BigDecimal.valueOf(billingDetails.getTestCharges());
//
//        BigDecimal totalDiscount =
//                billingDetails.getDiscountAmount() == null
//                        ? BigDecimal.ZERO
//                        : BigDecimal.valueOf(billingDetails.getDiscountAmount());
//
//        BigDecimal testGross =
//                BigDecimal.valueOf(testBilling.getTotalAmount());
//
//        BigDecimal refundAmount;
//
//        if (totalDiscount.compareTo(BigDecimal.ZERO) > 0) {
//
//            BigDecimal discountRatio = totalDiscount
//                    .divide(orderTotal, 6, BigDecimal.ROUND_HALF_UP);
//
//            BigDecimal discountShare = testGross
//                    .multiply(discountRatio);
//
//            refundAmount = testGross
//                    .subtract(discountShare)
//                    .setScale(2, BigDecimal.ROUND_HALF_UP);
//
//        } else {
//            refundAmount = testGross.setScale(2, BigDecimal.ROUND_HALF_UP);
//        }
//
//        // 6️⃣ Validate Refund Not Exceeding Paid
//        BigDecimal totalPaid =
//                BigDecimal.valueOf(billingDetails.getTotalPayment());
//
//        if (refundAmount.compareTo(totalPaid) > 0) {
//            throw new IllegalStateException("Refund exceeds paid amount");
//        }
//
//        // 7️⃣ Save Refund Entry
//        LabRefund refund = LabRefund.builder()
//                .labOrderId(labOrderId)
//                .billingId(billingMaster.getId())
//                .labStoreId(billingMaster.getLabStoreId())
//                .orderItemId(orderItemId)
//                .refundAmount(refundAmount.doubleValue())
//                .reason(reason)
//                .refundStatus(RefundStatus.PROCESSED)
//                .refundedAt(LocalDateTime.now())
//                .createdAt(LocalDateTime.now())
//                .build();
//
//        labRefundRepo.save(refund);
//
//        // 8️⃣ Update Billing Details
//
//        BigDecimal updatedPayment = totalPaid.subtract(refundAmount);
//        billingDetails.setTotalPayment(updatedPayment.doubleValue());
//
//        BigDecimal updatedDue = orderTotal.subtract(updatedPayment);
//        billingDetails.setDue(updatedDue.doubleValue());
//
//        billingDetails.setUpdatedAt(LocalDateTime.now());
//        labBillingRepo.save(billingDetails);
//
//        // 9️⃣ Derive Payment Status
//
//        BigDecimal totalRefunded =
//                BigDecimal.valueOf(
//                        labRefundRepo
//                                .sumProcessedRefundByBillingId(
//                                        billingMaster.getId()
//                                )
//                );
//
//        BigDecimal invoiceTotal =
//                BigDecimal.valueOf(billingMaster.getTotalAmount());
//
//        if (totalRefunded.compareTo(invoiceTotal) == 0) {
//            billingMaster.setPaymentStatus(PaymentStatus.REFUNDED);
//        }
//        else if (totalRefunded.compareTo(BigDecimal.ZERO) > 0) {
//            billingMaster.setPaymentStatus(PaymentStatus.PARTIALLY_REFUNDED);
//        }
//        else if (updatedDue.compareTo(BigDecimal.ZERO) > 0) {
//            billingMaster.setPaymentStatus(PaymentStatus.PARTIALLY_PAID);
//        }
//        else {
//            billingMaster.setPaymentStatus(PaymentStatus.PAID);
//        }
//
//        billingMaster.setUpdatedAt(LocalDateTime.now());
//        billingMasterRepo.save(billingMaster);
//
//        return "Refund processed successfully";
//    }

    @Override
    @Transactional
    public String processLabTestRefund(Long labOrderId,
                                       Long orderItemId,
                                       String reason) {

        if (labOrderId == null || orderItemId == null) {
            throw new IllegalArgumentException("Invalid refund request");
        }

        // 1️⃣ Fetch Billing Master
        BillingMaster billingMaster = billingMasterRepo
                .findByLabOrderId(labOrderId)
                .orElseThrow(() -> new RuntimeException("Billing not found"));

        // 2️⃣ Fetch Billing Details
        LabBillingDetails billingDetails = labBillingRepo
                .findByLabOrderIdAndBillingMaster_Id(
                        labOrderId,
                        billingMaster.getId())
                .orElseThrow(() -> new RuntimeException("Billing details not found"));

        // 3️⃣ Fetch Test Billing
        LabTestBilling testBilling = labTestBillingRepo
                .findByLabBillingDetails_IdAndOrderItemId(
                        billingDetails.getId(),
                        orderItemId)
                .orElseThrow(() -> new RuntimeException("Test billing not found"));

        // 4️⃣ Prevent Double Refund
        boolean alreadyRefunded = labRefundRepo
                .existsByBillingIdAndOrderItemIdAndRefundStatus(
                        billingMaster.getId(),
                        orderItemId,
                        RefundStatus.PROCESSED);

        if (alreadyRefunded) {
            throw new IllegalStateException("Test already refunded");
        }

        // =========================
        // 5️⃣ Financial Calculation
        // =========================

        BigDecimal testGross = testBilling.getTotalAmount() == null
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(testBilling.getTotalAmount());

        if (testGross.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Invalid test amount");
        }

        // 🔹 If discount applied at order level
        BigDecimal orderTotal = billingDetails.getTestCharges() == null
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(billingDetails.getTestCharges());

        BigDecimal totalDiscount = billingDetails.getDiscountAmount() == null
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(billingDetails.getDiscountAmount());

        BigDecimal refundAmount;

        if (totalDiscount.compareTo(BigDecimal.ZERO) > 0 && orderTotal.compareTo(BigDecimal.ZERO) > 0) {

            BigDecimal discountRatio = totalDiscount
                    .divide(orderTotal, 6, RoundingMode.HALF_UP);

            BigDecimal discountShare = testGross.multiply(discountRatio);

            refundAmount = testGross
                    .subtract(discountShare)
                    .setScale(2, RoundingMode.HALF_UP);

        } else {
            refundAmount = testGross.setScale(2, RoundingMode.HALF_UP);
        }

        // =========================
        // 6️⃣ Validate Payment Exists
        // =========================

        BigDecimal totalPaid = billingDetails.getTotalPayment() == null
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(billingDetails.getTotalPayment());

        if (refundAmount.compareTo(totalPaid) > 0) {
            throw new IllegalStateException("Refund exceeds paid amount");
        }

        // =========================
        // 7️⃣ Save Refund Entry
        // =========================

        LabRefund refund = LabRefund.builder()
                .labOrderId(labOrderId)
                .billingId(billingMaster.getId())
                .labStoreId(billingMaster.getLabStoreId())
                .orderItemId(orderItemId)
                .refundAmount(refundAmount.doubleValue())
                .reason(reason)
                .refundStatus(RefundStatus.PROCESSED)
                .refundedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        labRefundRepo.save(refund);

        // =========================
        // 8️⃣ Update Billing Details
        // =========================

        BigDecimal updatedPayment = totalPaid.subtract(refundAmount);
        billingDetails.setTotalPayment(updatedPayment.doubleValue());

        BigDecimal updatedDue = orderTotal.subtract(updatedPayment);
        billingDetails.setDue(updatedDue.doubleValue());

        billingDetails.setUpdatedAt(LocalDateTime.now());
        labBillingRepo.save(billingDetails);

        // =========================
        // 9️⃣ Update Payment Status
        // =========================

        Double refundedSum =
                labRefundRepo.sumProcessedRefundByBillingId(billingMaster.getId());

        BigDecimal totalRefunded = refundedSum == null
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(refundedSum);

        BigDecimal invoiceTotal = BigDecimal.valueOf(billingMaster.getTotalAmount());

        if (totalRefunded.compareTo(invoiceTotal) == 0) {
            billingMaster.setPaymentStatus(PaymentStatus.REFUNDED);
        }
        else if (totalRefunded.compareTo(BigDecimal.ZERO) > 0) {
            billingMaster.setPaymentStatus(PaymentStatus.PARTIALLY_REFUNDED);
        }
        else if (updatedDue.compareTo(BigDecimal.ZERO) > 0) {
            billingMaster.setPaymentStatus(PaymentStatus.PARTIALLY_PAID);
        }
        else {
            billingMaster.setPaymentStatus(PaymentStatus.PAID);
        }

        billingMaster.setUpdatedAt(LocalDateTime.now());
        billingMasterRepo.save(billingMaster);

        return "Refund processed successfully";
    }


    @Override
    public List<PaymentHistoryResponse> getPaymentHistory(Long orderId){
    	List<LabPayment> payments = labPaymentRepo.findByLabOrderIdOrderByPaidAtDesc(orderId);
    	
    	if(payments.isEmpty()) {
    		return List.of();
    	}
    	
       List<PaymentHistoryResponse> response = 
    			payments.stream()
    			.map(payment -> PaymentHistoryResponse.builder()
    					.paymentId(payment.getId())
    					.amount(payment.getAmount())
    					.paymentMode(payment.getPaymentMode().name())
    					.referenceNumber(payment.getReferenceNumber())
    					.paidAt(payment.getPaidAt())
    					.build()
    				)
    			.toList();
       
       return response;
    }
    
    @Override
    public List<PatientPaymentHistoryResponse> getPaymentHistoryByPatient(Long patientId){
    	
    	List<LabPayment> payments = labPaymentRepo.findByPatientExternalIdOrderByPaidAtDesc(patientId);
    	
    	if(payments.isEmpty()) {
    		return List.of();
    	}
    	
    	List<PatientPaymentHistoryResponse> response = payments.stream()
    			.map(payment -> PatientPaymentHistoryResponse.builder()
    					.paymentId(payment.getId())
    					.labOrderId(payment.getLabOrderId())
    					.billingId(payment.getBillingId())
    					.amount(payment.getAmount())
    					.paymentMode(payment.getPaymentMode().name())
    					.paidAt(payment.getPaidAt())
    					.build()
    				)
    			.toList();
    	return response;
    }



    @Override
    public BillingRevenueResponseDTO getRevenueSummary(
            RevenueSummaryRequest request
    ) {

        List<Object[]> rows = billingMasterRepo.getStoreWiseRevenue(
                request.getStoreIds(),
                request.getStartDate(),
                request.getEndDate()
        );

        double totalRevenue = 0;
        long totalOrders = 0;

        List<StoreRevenueDTO> storeWise = new ArrayList<>();

        for (Object[] r : rows) {

            Long storeId = ((Number) r[0]).longValue();
            double revenue = ((Number) r[1]).doubleValue();
            long orders = ((Number) r[2]).longValue();

            double avgOrder =
                    orders > 0 ? revenue / orders : 0.0;

            storeWise.add(
                    new StoreRevenueDTO(
                            storeId,
                            round(revenue),
                            orders,
                            round(avgOrder)
                    )
            );

            totalRevenue += revenue;
            totalOrders += orders;
        }

        double avgHospitalOrder =
                totalOrders > 0 ? totalRevenue / totalOrders : 0.0;

        return BillingRevenueResponseDTO.builder()
                .fromDate(request.getStartDate().toLocalDate().toString())
                .toDate(request.getEndDate().toLocalDate().toString())
                .summary(
                        new RevenueSummaryDTO(
                                round(totalRevenue),
                                totalOrders,
                                round(avgHospitalOrder)
                        )
                )
                .storeWise(storeWise)
                .build();
    }

    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

}
