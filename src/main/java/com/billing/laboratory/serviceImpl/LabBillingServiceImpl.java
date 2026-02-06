package com.billing.laboratory.serviceImpl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.billing.enums.PaymentStatus;
import com.billing.laboratory.dto.*;
import com.billing.laboratory.entity.LabBillingDetails;
import com.billing.laboratory.entity.LabTestBilling;
import com.billing.laboratory.repository.LabBillingDetailsRepository;
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
