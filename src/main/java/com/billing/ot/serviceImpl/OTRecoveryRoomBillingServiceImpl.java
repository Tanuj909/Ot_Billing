package com.billing.ot.serviceImpl;

import com.billing.enums.PaymentStatus;
import com.billing.exception.ResourceNotFoundException;
import com.billing.exception.ValidationException;
import com.billing.ot.dto.*;
import com.billing.ot.entity.OTBillingDetails;
import com.billing.ot.entity.OTRecoveryRoomBilling;
import com.billing.ot.repository.OTBillingDetailsRepository;
import com.billing.ot.repository.OTRecoveryRoomBillingRepository;
import com.billing.ot.service.OTBillingDetailsService;
import com.billing.ot.service.OTRecoveryRoomBillingService;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OTRecoveryRoomBillingServiceImpl implements OTRecoveryRoomBillingService {

    private final OTRecoveryRoomBillingRepository recoveryRoomRepository;
    private final OTBillingDetailsRepository otBillingDetailsRepository;
    private final OTBillingDetailsService otBillingDetailsService;
    private final EntityManager entityManager;

    private OTBillingDetails getDetails(Long operationId) {
        return otBillingDetailsRepository.findByOperationExternalId(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("OT Billing not found for operation: " + operationId));
    }

    private void validateBillingActive(OTBillingDetails details) {
        if (!"ACTIVE".equals(details.getBillingStatus())) {
            throw new ValidationException("Cannot proceed — billing is " + details.getBillingStatus());
        }
        if (details.getBillingMaster().getPaymentStatus().equals(PaymentStatus.CANCELLED)) {
            throw new ValidationException("Cannot proceed — billing master is CANCELLED");
        }
    }

    // ==================== CREATE ====================
    @Transactional
    @Override
    public OTRecoveryRoomBillingResponse createRecoveryRoom(OTRecoveryRoomBillingRequest request) {

        OTBillingDetails details = getDetails(request.getOperationExternalId());
        validateBillingActive(details);

        // Check if recovery room already exists
        if (recoveryRoomRepository.findByOtBillingDetails(details).isPresent()) {
            throw new ValidationException("Recovery room already exists for this operation");
        }

        OTRecoveryRoomBilling recovery = OTRecoveryRoomBilling.builder()
                .otBillingDetails(details)
                .wardRoomId(request.getWardRoomId())
                .wardRoomBedId(request.getWardRoomBedId())
                .wardRoomName(request.getWardRoomName())
                .startTime(request.getStartTime() != null ? request.getStartTime() : LocalDateTime.now())
                .ratePerHour(request.getRatePerHour())
                .discountPercent(request.getDiscountPercent())
                .gstPercent(request.getGstPercent())
                .build();

        recoveryRoomRepository.save(recovery);
        otBillingDetailsService.recalculateTotals(request.getOperationExternalId());

        return mapToResponse(recovery);
    }

    // ==================== UPDATE ====================
    @Transactional
    @Override
    public OTRecoveryRoomBillingResponse updateRecoveryRoom(Long recoveryId, OTRecoveryRoomBillingUpdateRequest request) {

        OTRecoveryRoomBilling recovery = recoveryRoomRepository.findById(recoveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Recovery room billing not found"));

        validateBillingActive(recovery.getOtBillingDetails());

        if (request.getStartTime() != null) recovery.setStartTime(request.getStartTime());
        if (request.getRatePerHour() != null) recovery.setRatePerHour(request.getRatePerHour());
        if (request.getDiscountPercent() != null) recovery.setDiscountPercent(request.getDiscountPercent());
        if (request.getGstPercent() != null) recovery.setGstPercent(request.getGstPercent());

        recovery.calculateAmounts();
        recoveryRoomRepository.save(recovery);

        otBillingDetailsService.recalculateTotals(recovery.getOtBillingDetails().getOperationExternalId());

        return mapToResponse(recovery);
    }

    // ==================== SET END TIME ====================
    @Transactional
    @Override
    public OTRecoveryRoomBillingResponse setEndTime(OTRecoveryRoomBillingEndRequest request) {

        OTBillingDetails details = getDetails(request.getOperationExternalId());

        OTRecoveryRoomBilling recovery = recoveryRoomRepository.findByOtBillingDetails(details)
                .orElseThrow(() -> new ResourceNotFoundException("Recovery room not found"));

        if (request.getEndTime().isBefore(recovery.getStartTime())) {
            throw new ValidationException("End time cannot be before start time");
        }

        recovery.setEndTime(request.getEndTime());
        recovery.calculateAmounts();
        recoveryRoomRepository.save(recovery);

        otBillingDetailsService.recalculateTotals(request.getOperationExternalId());

        return mapToResponse(recovery);
    }

    // ==================== REMOVE ====================
    @Transactional
    @Override
    public void removeRecoveryRoom(Long recoveryId) {

        OTRecoveryRoomBilling recovery = recoveryRoomRepository.findById(recoveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Recovery room billing not found"));

        validateBillingActive(recovery.getOtBillingDetails());

        Long operationId = recovery.getOtBillingDetails().getOperationExternalId();

        recoveryRoomRepository.delete(recovery);
        recoveryRoomRepository.flush();
        entityManager.clear();

        otBillingDetailsService.recalculateTotals(operationId);
    }

    // ==================== GET ====================
    @Override
    public OTRecoveryRoomBillingResponse getById(Long recoveryId) {
        return mapToResponse(recoveryRoomRepository.findById(recoveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Recovery room not found")));
    }

    @Override
    public List<OTRecoveryRoomBillingResponse> getByOperationId(Long operationId) {
        OTBillingDetails details = getDetails(operationId);
        return recoveryRoomRepository.findByOtBillingDetails(details)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OTRecoveryRoomBillingResponse getCurrentRecoveryRoom(Long operationId) {
        OTBillingDetails details = getDetails(operationId);
        OTRecoveryRoomBilling recovery = recoveryRoomRepository.findByOtBillingDetails(details)
                .orElseThrow(() -> new ResourceNotFoundException("No recovery room found"));

        return mapToResponse(recovery);
    }

    // ==================== MAPPER ====================
    private OTRecoveryRoomBillingResponse mapToResponse(OTRecoveryRoomBilling recovery) {
        return OTRecoveryRoomBillingResponse.builder()
                .id(recovery.getId())
                .otBillingDetailsId(recovery.getOtBillingDetails().getId())
                .operationExternalId(recovery.getOtBillingDetails().getOperationExternalId())
                .wardRoomId(recovery.getWardRoomId())
                .wardRoomBedId(recovery.getWardRoomBedId())
                .wardRoomName(recovery.getWardRoomName())
                .startTime(recovery.getStartTime())
                .endTime(recovery.getEndTime())
                .durationMinutes(recovery.getDurationMinutes())
                .totalHours(recovery.getTotalHours())
                .ratePerHour(recovery.getRatePerHour())
                .baseAmount(recovery.getBaseAmount())
                .discountPercent(recovery.getDiscountPercent())
                .discountAmount(recovery.getDiscountAmount())
                .priceAfterDiscount(recovery.getPriceAfterDiscount())
                .gstPercent(recovery.getGstPercent())
                .gstAmount(recovery.getGstAmount())
                .totalAmount(recovery.getTotalAmount())
                .isCurrent(recovery.getEndTime() == null)
                .createdAt(recovery.getCreatedAt())
                .updatedAt(recovery.getUpdatedAt())
                .build();
    }
}