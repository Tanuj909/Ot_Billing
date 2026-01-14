package com.billing.laboratory.serviceImpl;

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
    public GenerateLabBillResponse generateBill(GenerateLabBillRequest request) {

    	double totalAmount = AmountUtil.round(
    	        request.getTests()
    	            .stream()
    	            .mapToDouble(t ->
    	                t.getPrice() +
    	                (t.getGstAmount() != null ? t.getGstAmount().doubleValue() : 0)
    	            )
    	            .sum()
    	);


        BillingMaster billingMaster = BillingMaster.builder()
                .hospitaExternallId(request.getHospitalExternalId())
                .patientExternalId(request.getPatientExternalId())
                .labOrderId(request.getLabOrderId())
                .moduleType("LAB")
                .totalAmount(totalAmount)
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        billingMasterRepo.save(billingMaster);

        LabBillingDetails labBilling = new LabBillingDetails();
        labBilling.setLabOrderId(request.getLabOrderId());
        labBilling.setBillingMaster(billingMaster);
        labBilling.setTestCharges(totalAmount);
        labBilling.setTotalPayment(0.0);
        labBilling.setDue(totalAmount);

        labBillingRepo.save(labBilling);

        for (LabTestBillItemDTO dto : request.getTests()) {
            LabTestBilling testBilling = LabTestBilling.builder()
                    .labBillingDetails(labBilling)
                    .testName(dto.getTestName())
                    .price(dto.getPrice())
                    .gstPercentage(dto.getGstPercentage())
                    .gstAmount(dto.getGstAmount())
                    .totalAmount(dto.getPrice() + dto.getGstAmount().doubleValue())
                    .build();

            labTestBillingRepo.save(testBilling);
        }

        return GenerateLabBillResponse.builder()
                .billingId(billingMaster.getId())
                .labBillingId(labBilling.getId())
                .totalAmount(totalAmount)
                .dueAmount(totalAmount)
                .billingStatus("ACTIVE")
                .build();
    }

    @Override
    public void makePayment(LabPaymentRequest request) {

        LabBillingDetails billing = labBillingRepo.findById(request.getLabBillingId())
                .orElseThrow(() -> new RuntimeException("Lab Billing not found"));

        double paid = AmountUtil.round(
        	    billing.getTotalPayment() + request.getAmountPaid()
        	);

        	double due = AmountUtil.round(
        	    billing.getTestCharges() - paid
        	);

        	billing.setTotalPayment(paid);
        	billing.setDue(due);


        if (due <= 0) {
            billing.getBillingMaster().setPaymentStatus(PaymentStatus.PAID);
        } else {
            billing.getBillingMaster().setPaymentStatus(PaymentStatus.PARTIAL);
        }
    }

    @Override
    public void applyDiscount(LabDiscountRequest request) {

        LabBillingDetails billing = labBillingRepo.findById(request.getLabBillingId())
                .orElseThrow(() -> new RuntimeException("Lab Billing not found"));

        double originalAmount = billing.getTestCharges();
        double discountAmount = 0.0;

        if (request.getDiscountPercentage() != null) {
            discountAmount = (originalAmount * request.getDiscountPercentage()) / 100;
            billing.setDiscountPercentage(request.getDiscountPercentage());
        }

        if (request.getDiscountAmount() != null) {
            discountAmount = request.getDiscountAmount();
            billing.setDiscountPercentage(null);
        }

        discountAmount = AmountUtil.round(discountAmount);

        double newTotal = AmountUtil.round(originalAmount - discountAmount);

        billing.setDiscountAmount(discountAmount);
        billing.setTestCharges(newTotal);

        billing.setDue(
            AmountUtil.round(newTotal - billing.getTotalPayment())
        );
    }

    
    @Override
    public void removeDiscount(RemoveLabDiscountRequest request) {

        LabBillingDetails billing = labBillingRepo.findById(request.getLabBillingId())
                .orElseThrow(() -> new RuntimeException("Lab Billing not found"));

        if (billing.getDiscountAmount() == null || billing.getDiscountAmount() == 0) {
            return; // no discount applied
        }

        double restoredAmount = AmountUtil.round(
            billing.getTestCharges() + billing.getDiscountAmount()
        );

        billing.setTestCharges(restoredAmount);
        billing.setDiscountAmount(0.0);
        billing.setDiscountPercentage(null);

        billing.setDue(
            AmountUtil.round(restoredAmount - billing.getTotalPayment())
        );
    }


    @Override
    public LabBillResponse getBillByLabOrder(Long labOrderId) {

        LabBillingDetails billing = labBillingRepo.findByLabOrderId(labOrderId)
                .orElseThrow(() -> new RuntimeException("Bill not found"));

        List<LabTestBillItemDTO> tests =
                labTestBillingRepo.findByLabBillingDetailsId(billing.getId());

        return LabBillResponse.builder()
                .labBillingId(billing.getId())
                .billingId(billing.getBillingMaster().getId())
                .labOrderId(labOrderId)
                .totalAmount(billing.getTestCharges())
                .totalPaid(billing.getTotalPayment())
                .due(billing.getDue())
                .paymentStatus(billing.getBillingMaster().getPaymentStatus().name())
                .billingStatus(billing.getBillingStatus())
                .tests(tests)
                .build();
    }
}
