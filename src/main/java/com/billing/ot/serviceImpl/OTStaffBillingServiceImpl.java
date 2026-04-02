package com.billing.ot.serviceImpl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.billing.exception.ResourceNotFoundException;
import com.billing.exception.ValidationException;
import com.billing.ot.dto.OTStaffBillingRequest;
import com.billing.ot.dto.OTStaffBillingResponse;
import com.billing.ot.dto.OTStaffBillingUpdateRequest;
import com.billing.ot.entity.OTBillingDetails;
import com.billing.ot.entity.OTStaffBilling;
import com.billing.ot.repository.OTBillingDetailsRepository;
import com.billing.ot.repository.OTStaffBillingRepository;
import com.billing.ot.service.OTBillingDetailsService;
import com.billing.ot.service.OTStaffBillingService;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OTStaffBillingServiceImpl implements OTStaffBillingService {

    private final OTStaffBillingRepository staffBillingRepository;
    private final OTBillingDetailsRepository otBillingDetailsRepository;
    private final OTBillingDetailsService otBillingDetailsService;
    private final EntityManager entityManager;

    // ---------------------------------------- Add ---------------------------------------- //

    @Transactional
    @Override
    public OTStaffBillingResponse addStaffBilling(OTStaffBillingRequest request) {

        // OTBillingDetails fetch
        OTBillingDetails details = otBillingDetailsRepository
                .findByOperationExternalId(request.getOperationExternalId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "OT Billing not found for operation: " + request.getOperationExternalId()));

        // Billing ACTIVE check
        if (!details.getBillingStatus().equals("ACTIVE")) {
            throw new ValidationException("Cannot add staff — billing is " + details.getBillingStatus());
        }

        // Duplicate staff check
        if (staffBillingRepository.existsByOtBillingDetailsAndStaffExternalId(
                details, request.getStaffExternalId())) {
            throw new ValidationException("Staff already added to billing: "
                    + request.getStaffName());
        }

        OTStaffBilling staffBilling = OTStaffBilling.builder()
                .otBillingDetails(details)
                .staffExternalId(request.getStaffExternalId())
                .staffName(request.getStaffName())
                .staffRole(request.getStaffRole())
                .fees(request.getFees())
                .discountPercent(request.getDiscountPercent())
                .gstPercent(request.getGstPercent())
                .build();

        staffBillingRepository.save(staffBilling);

        // Totals recalculate
        otBillingDetailsService.recalculateTotals(request.getOperationExternalId());

        return mapToResponse(staffBilling);
    }

    // ----------------------------------------- Get ------------------------------------------ //

    @Override
    public List<OTStaffBillingResponse> getStaffBillingByOperationId(Long operationId) {

        OTBillingDetails details = otBillingDetailsRepository
                .findByOperationExternalId(operationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "OT Billing not found for operation: " + operationId));

        return staffBillingRepository.findByOtBillingDetails(details)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OTStaffBillingResponse getStaffBillingById(Long staffBillingId) {
        return mapToResponse(staffBillingRepository.findById(staffBillingId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff billing not found")));
    }

    // ---------------------------------------- Update ---------------------------------------- //
    @Transactional
    @Override
    public OTStaffBillingResponse updateStaffBilling(Long staffBillingId,
            OTStaffBillingUpdateRequest request) {

        OTStaffBilling staffBilling = staffBillingRepository.findById(staffBillingId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff billing not found"));

        // Billing ACTIVE check
        if (!staffBilling.getOtBillingDetails().getBillingStatus().equals("ACTIVE")) {
            throw new ValidationException("Cannot update — billing is "
                    + staffBilling.getOtBillingDetails().getBillingStatus());
        }

        if (request.getFees() != null)            staffBilling.setFees(request.getFees());
        if (request.getDiscountPercent() != null) staffBilling.setDiscountPercent(request.getDiscountPercent());
        if (request.getGstPercent() != null)      staffBilling.setGstPercent(request.getGstPercent());

        // Recalculate
        staffBilling.calculateAmounts();
        staffBillingRepository.save(staffBilling);

        // Totals recalculate
        otBillingDetailsService.recalculateTotals(
                staffBilling.getOtBillingDetails().getOperationExternalId());

        return mapToResponse(staffBilling);
    }

    // ---------------------------------------- Remove ---------------------------------------- //

    @Transactional
    @Override
    public void removeStaffBilling(Long staffBillingId) {

        OTStaffBilling staffBilling = staffBillingRepository.findById(staffBillingId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff billing not found"));

        // Billing ACTIVE check
        if (!staffBilling.getOtBillingDetails().getBillingStatus().equals("ACTIVE")) {
            throw new ValidationException("Cannot remove — billing is "
                    + staffBilling.getOtBillingDetails().getBillingStatus());
        }

        Long operationId = staffBilling.getOtBillingDetails().getOperationExternalId();

        staffBillingRepository.delete(staffBilling);
        staffBillingRepository.flush(); // ← Flush delete to DB first
        entityManager.clear();          // ← Clear session so no ghost references remain

        // Totals recalculate
        otBillingDetailsService.recalculateTotals(operationId);
    }

    // ---------------------------------------- Mapper ---------------------------------------- //

    private OTStaffBillingResponse mapToResponse(OTStaffBilling staffBilling) {
        return OTStaffBillingResponse.builder()
                .id(staffBilling.getId())
                .otBillingDetailsId(staffBilling.getOtBillingDetails().getId())
                .staffExternalId(staffBilling.getStaffExternalId())
                .staffName(staffBilling.getStaffName())
                .staffRole(staffBilling.getStaffRole())
                .fees(staffBilling.getFees())
                .discountPercent(staffBilling.getDiscountPercent())
                .discountAmount(staffBilling.getDiscountAmount())
                .priceAfterDiscount(staffBilling.getPriceAfterDiscount())
                .gstPercent(staffBilling.getGstPercent())
                .gstAmount(staffBilling.getGstAmount())
                .totalAmount(staffBilling.getTotalAmount())
                .serviceAddedAt(staffBilling.getServiceAddedAt())
                .createdAt(staffBilling.getCreatedAt())
                .build();
    }
}
