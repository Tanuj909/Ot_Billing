package com.billing.ot.serviceImpl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.billing.enums.PaymentStatus;
import com.billing.exception.ResourceNotFoundException;
import com.billing.exception.StatusException;
import com.billing.exception.ValidationException;
import com.billing.model.BillingMaster;
import com.billing.ot.dto.OTBillingDetailsRequest;
import com.billing.ot.dto.OTBillingDetailsResponse;
import com.billing.ot.dto.OTBillingSummaryResponse;
import com.billing.ot.dto.OTItemBillingResponse;
import com.billing.ot.dto.OTRoomBillingResponse;
import com.billing.ot.dto.OTStaffBillingResponse;
import com.billing.ot.entity.OTBillingDetails;
import com.billing.ot.mapper.OTBillingMapper;
import com.billing.ot.repository.OTBillingDetailsRepository;
import com.billing.ot.repository.OTItemBillingRepository;
import com.billing.ot.repository.OTPaymentRepository;
import com.billing.ot.repository.OTRefundRepository;
import com.billing.ot.repository.OTRoomBillingRepository;
import com.billing.ot.repository.OTStaffBillingRepository;
import com.billing.ot.service.OTBillingDetailsService;
import com.billing.repository.BillingMasterRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OTBillingDetailsServiceImpl implements OTBillingDetailsService {

    private final OTBillingDetailsRepository otBillingDetailsRepository;
    private final BillingMasterRepository billingMasterRepository;
    private final OTPaymentRepository paymentRepository;
    private final OTRefundRepository refundRepository;
    private final OTStaffBillingRepository staffBillingRepository;
    private final OTRoomBillingRepository roomBillingRepository;
    private final OTItemBillingRepository itemBillingRepository;
    private final OTBillingMapper otBillingMapper;  // 👈 inject
    

    // ---------------------------------------- Create ---------------------------------------- //

    //Surgery Start hone pai call krni hai!
    @Transactional
    @Override
    public OTBillingDetailsResponse createOTBillingDetails(OTBillingDetailsRequest request) {

        // BillingMaster fetch
        BillingMaster billingMaster = billingMasterRepository.findById(request.getBillingMasterId())
                .orElseThrow(() -> new ResourceNotFoundException("Billing master not found"));
        
        // Duplicate check
//        if (otBillingDetailsRepository.existsByOperationExternalId(billingMaster.getOtOperationId())) {
//            throw new ValidationException("OT Billing already exists for operation: "
//                    + billingMaster.getOtOperationId());
//        }
        
     // ✅ Check if already exists (idempotent behavior)
        OTBillingDetails existing = otBillingDetailsRepository
                .findByOperationExternalId(billingMaster.getOtOperationId())
                .orElse(null);

        if (existing != null) {
            return mapToResponse(existing); // 🔥 IMPORTANT
        }
        
        // 👇 BillingMaster status check — CANCELLED billing pe details nahi ban sakti
        if (billingMaster.getPaymentStatus().equals(PaymentStatus.CANCELLED)) {
            throw new ValidationException("Cannot create OT billing — billing master is CANCELLED");
        }

        // 👇 COMPLETED billing pe bhi details nahi ban sakti
        if (billingMaster.getPaymentStatus().equals(PaymentStatus.COMPLETED)) {
            throw new ValidationException("Cannot create OT billing — billing master is already COMPLETED");
        }

        OTBillingDetails details = OTBillingDetails.builder()
                .billingMaster(billingMaster)
                .operationExternalId(billingMaster.getOtOperationId())
                .operationReference(request.getOperationReference())
                .hospitalExternalId(billingMaster.getHospitaExternallId())
                .patientExternalId(billingMaster.getPatientExternalId())
                .billingStatus("ACTIVE")
                .build();

        otBillingDetailsRepository.save(details);
        return mapToResponse(details);
    }

    // ----------------------------------------- Get ------------------------------------------ //

    @Override
    public OTBillingDetailsResponse getByOperationId(Long operationId) {
        return mapToResponse(otBillingDetailsRepository
                .findByOperationExternalId(operationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "OT Billing not found for operation: " + operationId)));
    }

    @Override
    public OTBillingDetailsResponse getById(Long id) {
        return mapToResponse(otBillingDetailsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OT Billing not found")));
    }

    // ---------------------------------------- Recalculate ---------------------------------------- //

    @Transactional
    @Override
    public OTBillingDetailsResponse recalculateTotals(Long operationId) {

        OTBillingDetails details = otBillingDetailsRepository
                .findByOperationExternalId(operationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "OT Billing not found for operation: " + operationId));
        
//        //Check if Bill is ACTIVE?
//        if(!details.getBillingStatus().equals("ACTIVE")) {
//        	throw new StatusException("Billing Details Not Active");
//        }

        // Staff total
        double totalStaff = details.getStaffCharges().stream()
                .mapToDouble(s -> s.getTotalAmount() != null ? s.getTotalAmount() : 0.0)
                .sum();

        // Room total
        double totalRoom = details.getRoomCharges() != null
                && details.getRoomCharges().getTotalAmount() != null
                ? details.getRoomCharges().getTotalAmount() : 0.0;

        // Items total
        double totalItems = details.getItemCharges().stream()
                .mapToDouble(i -> i.getTotalAmount() != null ? i.getTotalAmount() : 0.0)
                .sum();

        // Discount total
        double totalDiscount = details.getStaffCharges().stream()
                .mapToDouble(s -> s.getDiscountAmount() != null ? s.getDiscountAmount() : 0.0)
                .sum()
                + (details.getRoomCharges() != null
                        && details.getRoomCharges().getDiscountAmount() != null
                        ? details.getRoomCharges().getDiscountAmount() : 0.0)
                + details.getItemCharges().stream()
                        .mapToDouble(i -> i.getDiscountAmount() != null ? i.getDiscountAmount() : 0.0)
                        .sum();

        // GST total
        double totalGst = details.getStaffCharges().stream()
                .mapToDouble(s -> s.getGstAmount() != null ? s.getGstAmount() : 0.0)
                .sum()
                + (details.getRoomCharges() != null
                        && details.getRoomCharges().getGstAmount() != null
                        ? details.getRoomCharges().getGstAmount() : 0.0)
                + details.getItemCharges().stream()
                        .mapToDouble(i -> i.getGstAmount() != null ? i.getGstAmount() : 0.0)
                        .sum();

        double grossAmount = totalStaff + totalRoom + totalItems;
        double totalAmount = grossAmount;
        double due = totalAmount - (details.getAdvancePaid() != null ? details.getAdvancePaid() : 0.0);

        // Update
        details.setTotalStaffCharges(totalStaff);
        details.setTotalRoomCharges(totalRoom);
        details.setTotalItemCharges(totalItems);
        details.setTotalDiscountAmount(totalDiscount);
        details.setTotalGstAmount(totalGst);
        details.setGrossAmount(grossAmount);
        details.setTotalAmount(totalAmount);
        details.setDue(due);

        // BillingMaster bhi update karo
        BillingMaster billingMaster = details.getBillingMaster();
        billingMaster.setTotalAmount(totalAmount);
        billingMasterRepository.save(billingMaster);

        otBillingDetailsRepository.save(details);
        return mapToResponse(details);
    }

    // ---------------------------------------- Close ---------------------------------------- //

    @Transactional
    @Override
    public OTBillingDetailsResponse closeBilling(Long operationId) {

        OTBillingDetails details = otBillingDetailsRepository
                .findByOperationExternalId(operationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "OT Billing not found for operation: " + operationId));

        if (details.getBillingStatus().equals("CLOSED")) {
            throw new ValidationException("Billing is already closed");
        }

        if (details.getBillingStatus().equals("CANCELLED")) {
            throw new ValidationException("Cancelled billing cannot be closed");
        }
        
//        if (details.getTotalAmount() == null || details.getTotalAmount() <= 0) {
//            throw new ValidationException("Cannot close billing — total amount is zero");
//        }

        details.setBillingStatus("CLOSED");

        // BillingMaster status update
//        BillingMaster billingMaster = details.getBillingMaster();
//        billingMaster.setPaymentStatus(PaymentStatus.COMPLETED);
//        billingMasterRepository.save(billingMaster);

        otBillingDetailsRepository.save(details);
        return mapToResponse(details);
    }

    // ---------------------------------------- Mapper ---------------------------------------- //

    private OTBillingDetailsResponse mapToResponse(OTBillingDetails details) {
        return OTBillingDetailsResponse.builder()
                .id(details.getId())
                .billingMasterId(details.getBillingMaster().getId())
                .operationExternalId(details.getOperationExternalId())
                .operationReference(details.getOperationReference())
                .hospitalExternalId(details.getHospitalExternalId())
                .patientExternalId(details.getPatientExternalId())
                .totalStaffCharges(details.getTotalStaffCharges())
                .totalRoomCharges(details.getTotalRoomCharges())
                .totalItemCharges(details.getTotalItemCharges())
                .totalDiscountAmount(details.getTotalDiscountAmount())
                .totalGstAmount(details.getTotalGstAmount())
                .grossAmount(details.getGrossAmount())
                .totalAmount(details.getTotalAmount())
                .advancePaid(details.getAdvancePaid())
                .due(details.getDue())
                .billingStatus(details.getBillingStatus())
                .createdAt(details.getCreatedAt())
                .updatedAt(details.getUpdatedAt())
                .build();
    }
    
    
    
    @Override
    public OTBillingSummaryResponse getBillingSummary(Long operationId) {

        // 1. OTBillingDetails fetch
        OTBillingDetails details = otBillingDetailsRepository
                .findByOperationExternalId(operationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "OT Billing not found for operation: " + operationId));

        BillingMaster billingMaster = details.getBillingMaster();

        // 2. Staff charges
        List<OTStaffBillingResponse> staffList = staffBillingRepository
                .findByOtBillingDetails(details)
                .stream()
                .map(otBillingMapper::mapStaff)
                .collect(Collectors.toList());

        double totalStaff = staffList.stream()
                .mapToDouble(s -> s.getTotalAmount() != null ? s.getTotalAmount() : 0.0)
                .sum();

        // 3. Room charges
        List<OTRoomBillingResponse> roomList = roomBillingRepository
                .findAllByOtBillingDetails(details)
                .stream()
                .map(otBillingMapper::mapRoom)
                .collect(Collectors.toList());

        double totalRoom = roomList.stream()
                .mapToDouble(r -> r.getTotalAmount() != null ? r.getTotalAmount() : 0.0)
                .sum();

        // 4. Item charges
        List<OTItemBillingResponse> itemList = itemBillingRepository
                .findByOtBillingDetails(details)
                .stream()
                .map(otBillingMapper::mapItem)
                .collect(Collectors.toList());

        double totalItems = itemList.stream()
                .mapToDouble(i -> i.getTotalAmount() != null ? i.getTotalAmount() : 0.0)
                .sum();

        // Group by itemType
        Map<String, Double> totalByType = itemList.stream()
                .collect(Collectors.groupingBy(
                        i -> i.getItemType().name(),
                        Collectors.summingDouble(i -> i.getTotalAmount() != null
                                ? i.getTotalAmount() : 0.0)));

        // 5. Payments
        List<OTBillingSummaryResponse.PaymentEntry> payments = paymentRepository
                .findByOtBillingDetails(details)
                .stream()
                .map(p -> OTBillingSummaryResponse.PaymentEntry.builder()
                        .id(p.getId())
                        .paymentType(p.getPaymentType().name())
                        .paymentMode(p.getPaymentMode().name())
                        .amount(p.getAmount())
                        .status(p.getStatus().name())
                        .referenceNumber(p.getReferenceNumber())
                        .receivedBy(p.getReceivedBy())
                        .notes(p.getNotes())
                        .paidAt(p.getPaidAt())
                        .build())
                .collect(Collectors.toList());

        // 6. Refunds
        List<OTBillingSummaryResponse.RefundEntry> refunds = refundRepository
                .findByOtBillingDetails(details)
                .stream()
                .map(r -> OTBillingSummaryResponse.RefundEntry.builder()
                        .id(r.getId())
                        .paymentId(r.getOtPayment().getId())
                        .refundAmount(r.getRefundAmount())
                        .reason(r.getReason())
                        .refundMode(r.getRefundMode().name())
                        .refundStatus(r.getRefundStatus().name())
                        .processedBy(r.getProcessedBy())
                        .refundedAt(r.getRefundedAt())
                        .build())
                .collect(Collectors.toList());

        // 7. Total paid — sirf SUCCESS payments
        double totalPaid = payments.stream()
                .filter(p -> p.getStatus().equals("SUCCESS"))
                .mapToDouble(OTBillingSummaryResponse.PaymentEntry::getAmount)
                .sum();

        // 8. Total refunded — sirf COMPLETED refunds
        double totalRefunded = refunds.stream()
                .filter(r -> r.getRefundStatus().equals("COMPLETED"))
                .mapToDouble(OTBillingSummaryResponse.RefundEntry::getRefundAmount)
                .sum();

        return OTBillingSummaryResponse.builder()
                .operationExternalId(operationId)
                .operationReference(details.getOperationReference())
                .patientExternalId(details.getPatientExternalId())
                .hospitalExternalId(details.getHospitalExternalId())
                .billingMasterId(billingMaster.getId())
                .paymentStatus(billingMaster.getPaymentStatus().name())
                .paymentMode(billingMaster.getPaymentMode() != null
                        ? billingMaster.getPaymentMode().name() : null)
                .billingDate(billingMaster.getBillingDate())
                .staffCharges(OTBillingSummaryResponse.StaffChargesSummary.builder()
                        .totalAmount(totalStaff)
                        .staff(staffList)
                        .build())
                .roomCharges(OTBillingSummaryResponse.RoomChargesSummary.builder()
                        .totalAmount(totalRoom)
                        .rooms(roomList)
                        .build())
                .itemCharges(OTBillingSummaryResponse.ItemChargesSummary.builder()
                        .totalAmount(totalItems)
                        .totalByType(totalByType)
                        .items(itemList)
                        .build())
                .grossAmount(details.getGrossAmount())
                .totalDiscountAmount(details.getTotalDiscountAmount())
                .totalGstAmount(details.getTotalGstAmount())
                .totalAmount(details.getTotalAmount())
                .totalPaid(totalPaid)
                .totalRefunded(totalRefunded)
                .due(details.getDue())
                .billingStatus(details.getBillingStatus())
                .payments(payments)
                .refunds(refunds)
                .createdAt(details.getCreatedAt())
                .updatedAt(details.getUpdatedAt())
                .build();
    }
}