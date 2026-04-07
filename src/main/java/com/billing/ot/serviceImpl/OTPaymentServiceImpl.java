package com.billing.ot.serviceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import com.billing.enums.PaymentStatus;
import com.billing.exception.ResourceNotFoundException;
import com.billing.exception.ValidationException;
import com.billing.model.BillingMaster;
import com.billing.ot.dto.OTAdvancePaymentRequest;
import com.billing.ot.dto.OTPaymentHistoryResponse;
import com.billing.ot.dto.OTPaymentRequest;
import com.billing.ot.dto.OTPaymentResponse;
import com.billing.ot.dto.OTRefundRequest;
import com.billing.ot.dto.OTRefundResponse;
import com.billing.ot.entity.OTBillingDetails;
import com.billing.ot.entity.OTPayment;
import com.billing.ot.entity.OTRefund;
import com.billing.ot.enums.OTPaymentStatus;
import com.billing.ot.enums.OTPaymentType;
import com.billing.ot.enums.OTRefundStatus;
import com.billing.ot.repository.OTBillingDetailsRepository;
import com.billing.ot.repository.OTPaymentRepository;
import com.billing.ot.repository.OTRefundRepository;
import com.billing.ot.service.OTPaymentService;
import com.billing.repository.BillingMasterRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OTPaymentServiceImpl implements OTPaymentService {

    private final OTPaymentRepository paymentRepository;
    private final OTRefundRepository refundRepository;
    private final OTBillingDetailsRepository otBillingDetailsRepository;
    private final BillingMasterRepository billingMasterRepository;

    private OTBillingDetails getDetails(Long operationId) {
        return otBillingDetailsRepository
                .findByOperationExternalId(operationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "OT Billing not found for operation: " + operationId));
    }
    
    // ---------------------------------------- Advance Payment ---------------------------------------- //
    @Transactional
    @Override
    public OTPaymentResponse makeAdvancePayment(OTAdvancePaymentRequest request) {

        // 1. Fetch BillingMaster
        BillingMaster billingMaster = billingMasterRepository.findById(request.getBillingMasterId())
                .orElseThrow(() -> new ResourceNotFoundException("Billing master not found"));

        if (billingMaster.getPaymentStatus().equals(PaymentStatus.CANCELLED)) {
            throw new ValidationException("Cannot make advance — billing is CANCELLED");
        }

        // 2. Ensure OTBillingDetails exists (IMPORTANT 🔥)
        OTBillingDetails details = otBillingDetailsRepository
                .findByOperationExternalId(request.getOperationExternalId())
                .orElseGet(() -> {
                    OTBillingDetails newDetails = OTBillingDetails.builder()
                            .billingMaster(billingMaster)
                            .operationExternalId(billingMaster.getOtOperationId())
                            .hospitalExternalId(billingMaster.getHospitaExternallId())
                            .patientExternalId(billingMaster.getPatientExternalId())
                            .billingStatus("ACTIVE")
                            .build();

                    return otBillingDetailsRepository.save(newDetails);
                });

        // 3. Validate amount
        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new ValidationException("Advance amount must be greater than 0");
        }

        // 4. Create Payment
        OTPayment payment = OTPayment.builder()
                .otBillingDetails(details)
                .patientExternalId(billingMaster.getPatientExternalId())
                .paymentType(OTPaymentType.ADVANCE)
                .paymentMode(request.getPaymentMode())
                .amount(request.getAmount())
                .referenceNumber(request.getReferenceNumber())
                .receivedBy(request.getReceivedBy())
                .status(OTPaymentStatus.SUCCESS)
                .notes(request.getNotes() != null ? request.getNotes() : "Advance payment")
                .build();

        paymentRepository.save(payment);

        // 5. Update BillingDetails
        double newAdvance = (details.getAdvancePaid() != null
                ? details.getAdvancePaid() : 0.0) + request.getAmount();

        details.setAdvancePaid(newAdvance);

        // totalAmount abhi 0 ho sakta hai → due negative na ho
        double total = details.getTotalAmount() != null ? details.getTotalAmount() : 0.0;
        details.setDue(Math.max(total - newAdvance, 0.0));

        otBillingDetailsRepository.save(details);

        // 6. Update BillingMaster
        billingMaster.setPaymentStatus(PaymentStatus.PARTIALLY_PAID);
        billingMasterRepository.save(billingMaster);

        return mapToPaymentResponse(payment);
    }

    // ---------------------------------------- Payment ---------------------------------------- //

    @Transactional
    @Override
    public OTPaymentResponse makePayment(OTPaymentRequest request) {

        OTBillingDetails details = getDetails(request.getOperationExternalId());

        // CANCELLED check
        if (details.getBillingMaster().getPaymentStatus().equals(PaymentStatus.CANCELLED)) {
            throw new ValidationException("Cannot make payment — billing is CANCELLED");
        }

        // Amount validation
        if (request.getAmount() <= 0) {
            throw new ValidationException("Payment amount must be greater than 0");
        }

        // Overpayment check
        double due = details.getDue() != null ? details.getDue() : 0.0;
        if (request.getAmount() > due) {
            throw new ValidationException("Payment amount cannot exceed due amount: " + due);
        }

        // Payment type auto detect
        OTPaymentType paymentType = request.getPaymentType();
        if (paymentType == null) {
            if (request.getAmount() >= due) {
                paymentType = OTPaymentType.FULL;
            } else if (details.getAdvancePaid() != null && details.getAdvancePaid() > 0) {
                paymentType = OTPaymentType.PARTIAL;
            } else {
                paymentType = OTPaymentType.ADVANCE;
            }
        }

        OTPayment payment = OTPayment.builder()
                .otBillingDetails(details)
                .patientExternalId(request.getPatientExternalId())
                .paymentType(paymentType)
                .paymentMode(request.getPaymentMode())
                .amount(request.getAmount())
                .referenceNumber(request.getReferenceNumber())
                .receivedBy(request.getReceivedBy())
                .status(OTPaymentStatus.SUCCESS)
                .notes(request.getNotes())
                .build();

        paymentRepository.save(payment);

        // Update advancePaid and due
        double newAdvancePaid = (details.getAdvancePaid() != null
                ? details.getAdvancePaid() : 0.0) + request.getAmount();
        double newDue = details.getTotalAmount() - newAdvancePaid;

        details.setAdvancePaid(newAdvancePaid);
        details.setDue(Math.max(newDue, 0.0));
        otBillingDetailsRepository.save(details);

        // BillingMaster PaymentStatus update
        BillingMaster billingMaster = details.getBillingMaster();
        if (newDue <= 0) {
            billingMaster.setPaymentStatus(PaymentStatus.PAID);
        } else if (newAdvancePaid > 0) {
            billingMaster.setPaymentStatus(PaymentStatus.PARTIALLY_PAID);
        }
        billingMasterRepository.save(billingMaster);

        return mapToPaymentResponse(payment);
    }

    // ---------------------------------------- Refund ---------------------------------------- //

    @Transactional
    @Override
    public OTRefundResponse initiateRefund(OTRefundRequest request) {

        OTBillingDetails details = getDetails(request.getOperationExternalId());

        // Payment fetch
        OTPayment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        // Payment SUCCESS check
        if (!payment.getStatus().equals(OTPaymentStatus.SUCCESS)) {
            throw new ValidationException("Cannot refund — payment status is " + payment.getStatus());
        }

        // Refund amount validation
        if (request.getRefundAmount() <= 0) {
            throw new ValidationException("Refund amount must be greater than 0");
        }

        if (request.getRefundAmount() > payment.getAmount()) {
            throw new ValidationException("Refund amount cannot exceed payment amount: "
                    + payment.getAmount());
        }

        // Reason mandatory
        if (request.getReason() == null || request.getReason().isBlank()) {
            throw new ValidationException("Reason is mandatory for refund");
        }

        OTRefund refund = OTRefund.builder()
                .otBillingDetails(details)
                .otPayment(payment)
                .refundAmount(request.getRefundAmount())
                .reason(request.getReason())
                .refundMode(request.getRefundMode())
                .referenceNumber(request.getReferenceNumber())
                .processedBy(request.getProcessedBy())
                .refundStatus(OTRefundStatus.INITIATED)
                .build();

        refundRepository.save(refund);

        // Payment status update
        if (request.getRefundAmount() >= payment.getAmount()) {
            payment.setStatus(OTPaymentStatus.REFUNDED);
        } else {
            payment.setStatus(OTPaymentStatus.PARTIALLY_REFUNDED);
        }
        paymentRepository.save(payment);

        // Update advancePaid and due
        double newAdvancePaid = (details.getAdvancePaid() != null
                ? details.getAdvancePaid() : 0.0) - request.getRefundAmount();
        double newDue = details.getTotalAmount() - newAdvancePaid;

        details.setAdvancePaid(Math.max(newAdvancePaid, 0.0));
        details.setDue(newDue);
        otBillingDetailsRepository.save(details);

        // BillingMaster update
        BillingMaster billingMaster = details.getBillingMaster();
        billingMaster.setPaymentStatus(PaymentStatus.PARTIALLY_REFUNDED);
        billingMasterRepository.save(billingMaster);

        return mapToRefundResponse(refund);
    }

    @Transactional
    @Override
    public OTRefundResponse completeRefund(Long refundId) {

        OTRefund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new ResourceNotFoundException("Refund not found"));

        if (!refund.getRefundStatus().equals(OTRefundStatus.INITIATED)) {
            throw new ValidationException("Refund is already " + refund.getRefundStatus());
        }

        refund.setRefundStatus(OTRefundStatus.COMPLETED);
        refund.setRefundedAt(LocalDateTime.now());
        refundRepository.save(refund);

        // BillingMaster update
        BillingMaster billingMaster = refund.getOtBillingDetails().getBillingMaster();
        billingMaster.setPaymentStatus(PaymentStatus.REFUNDED);
        billingMasterRepository.save(billingMaster);

        return mapToRefundResponse(refund);
    }

    // ---------------------------------------- History ---------------------------------------- //

    @Override
    public OTPaymentHistoryResponse getPaymentHistory(Long operationId) {

        OTBillingDetails details = getDetails(operationId);

        List<OTPaymentResponse> payments = paymentRepository
                .findByOtBillingDetails(details)
                .stream()
                .map(this::mapToPaymentResponse)
                .collect(Collectors.toList());

        List<OTRefundResponse> refunds = refundRepository
                .findByOtBillingDetails(details)
                .stream()
                .map(this::mapToRefundResponse)
                .collect(Collectors.toList());

        // Total paid
        Double totalPaid = payments.stream()
                .filter(p -> p.getStatus().equals(OTPaymentStatus.SUCCESS))
                .mapToDouble(OTPaymentResponse::getAmount)
                .sum();

        // Total refunded
        Double totalRefunded = refunds.stream()
                .filter(r -> r.getRefundStatus().equals(OTRefundStatus.COMPLETED))
                .mapToDouble(OTRefundResponse::getRefundAmount)
                .sum();

        return OTPaymentHistoryResponse.builder()
                .operationExternalId(operationId)
                .operationReference(details.getOperationReference())
                .totalAmount(details.getTotalAmount())
                .totalPaid(totalPaid)
                .totalRefunded(totalRefunded)
                .due(details.getDue())
                .billingPaymentStatus(details.getBillingMaster().getPaymentStatus())
                .payments(payments)
                .refunds(refunds)
                .build();
    }

    // ---------------------------------------- Mappers ---------------------------------------- //

    private OTPaymentResponse mapToPaymentResponse(OTPayment payment) {
        return OTPaymentResponse.builder()
                .id(payment.getId())
                .otBillingDetailsId(payment.getOtBillingDetails().getId())
                .patientExternalId(payment.getPatientExternalId())
                .paymentType(payment.getPaymentType())
                .paymentMode(payment.getPaymentMode())
                .amount(payment.getAmount())
                .referenceNumber(payment.getReferenceNumber())
                .receivedBy(payment.getReceivedBy())
                .status(payment.getStatus())
                .notes(payment.getNotes())
                .paidAt(payment.getPaidAt())
                .createdAt(payment.getCreatedAt())
                .build();
    }

    private OTRefundResponse mapToRefundResponse(OTRefund refund) {
        return OTRefundResponse.builder()
                .id(refund.getId())
                .otBillingDetailsId(refund.getOtBillingDetails().getId())
                .paymentId(refund.getOtPayment().getId())
                .refundAmount(refund.getRefundAmount())
                .reason(refund.getReason())
                .refundMode(refund.getRefundMode())
                .referenceNumber(refund.getReferenceNumber())
                .processedBy(refund.getProcessedBy())
                .refundStatus(refund.getRefundStatus())
                .refundedAt(refund.getRefundedAt())
                .createdAt(refund.getCreatedAt())
                .build();
    }
}