package com.billing.ot.serviceImpl;

import com.billing.enums.PaymentStatus;
import com.billing.exception.ResourceNotFoundException;
import com.billing.exception.ValidationException;
import com.billing.ot.dto.OTDoctorVisitBillingRequest;
import com.billing.ot.dto.OTDoctorVisitBillingResponse;
import com.billing.ot.dto.OTDoctorVisitBillingUpdateRequest;
import com.billing.ot.entity.OTBillingDetails;
import com.billing.ot.entity.OTDoctorVisitBilling;
import com.billing.ot.repository.OTBillingDetailsRepository;
import com.billing.ot.repository.OTDoctorVisitBillingRepository;
import com.billing.ot.service.OTBillingDetailsService;
import com.billing.ot.service.OTDoctorVisitBillingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OTDoctorVisitBillingServiceImpl implements OTDoctorVisitBillingService {

    private final OTDoctorVisitBillingRepository doctorVisitBillingRepository;
    private final OTBillingDetailsRepository otBillingDetailsRepository;
    private final OTBillingDetailsService otBillingDetailsService;

    // ── Helpers ────────────────────────────────────────────────────────────

    private OTBillingDetails getDetails(Long operationId) {
        return otBillingDetailsRepository
                .findByOperationExternalId(operationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "OT Billing not found for operation: " + operationId));
    }

    private void validateBillingActive(OTBillingDetails details) {
        if (!"ACTIVE".equals(details.getBillingStatus())) {
            throw new ValidationException(
                    "Cannot proceed — billing is " + details.getBillingStatus());
        }
        if (details.getBillingMaster().getPaymentStatus().equals(PaymentStatus.CANCELLED)) {
            throw new ValidationException("Cannot proceed — billing master is CANCELLED");
        }
    }

    // ── Add ────────────────────────────────────────────────────────────────

    @Transactional
    @Override
    public OTDoctorVisitBillingResponse addDoctorVisit(OTDoctorVisitBillingRequest request) {

        OTBillingDetails details = getDetails(request.getOperationExternalId());
        validateBillingActive(details);

        // Fees validation
        if (request.getFees() == null || request.getFees() < 0) {
            throw new ValidationException("Fees must be a positive value");
        }

        OTDoctorVisitBilling visit = OTDoctorVisitBilling.builder()
                .otBillingDetails(details)
                .doctorExternalId(request.getDoctorExternalId())
                .doctorName(request.getDoctorName())
                .visitTime(request.getVisitTime() != null
                        ? request.getVisitTime()
                        : LocalDateTime.now())
                .fees(request.getFees())
                .build();

        doctorVisitBillingRepository.save(visit);

        // Totals recalculate — totalDoctorVisitCharges + totalAmount update hoga
        otBillingDetailsService.recalculateTotals(request.getOperationExternalId());

        return mapToResponse(visit);
    }

    // ── Update ─────────────────────────────────────────────────────────────

    @Transactional
    @Override
    public OTDoctorVisitBillingResponse updateDoctorVisit(Long visitBillingId,
            OTDoctorVisitBillingUpdateRequest request) {

        OTDoctorVisitBilling visit = doctorVisitBillingRepository.findById(visitBillingId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor visit billing not found"));

        validateBillingActive(visit.getOtBillingDetails());

        if (request.getFees() != null) {
            if (request.getFees() < 0) {
                throw new ValidationException("Fees cannot be negative");
            }
            visit.setFees(request.getFees());
        }
        if (request.getVisitTime() != null) {
            visit.setVisitTime(request.getVisitTime());
        }

        doctorVisitBillingRepository.save(visit);

        otBillingDetailsService.recalculateTotals(
                visit.getOtBillingDetails().getOperationExternalId());

        return mapToResponse(visit);
    }

    // ── Remove ─────────────────────────────────────────────────────────────

    @Transactional
    @Override
    public void removeDoctorVisit(Long visitBillingId) {

        OTDoctorVisitBilling visit = doctorVisitBillingRepository.findById(visitBillingId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor visit billing not found"));

        OTBillingDetails details = visit.getOtBillingDetails();
        validateBillingActive(details);

        Long operationId = details.getOperationExternalId();

        // orphanRemoval = true hai toh parent list se remove karo
        details.getDoctorVisits().remove(visit);

        doctorVisitBillingRepository.flush();

        otBillingDetailsService.recalculateTotals(operationId);
    }

    // ── Get ────────────────────────────────────────────────────────────────

    @Override
    public List<OTDoctorVisitBillingResponse> getByOperationId(Long operationId) {
        OTBillingDetails details = getDetails(operationId);
        return doctorVisitBillingRepository
                .findByOtBillingDetailsOrderByVisitTimeDesc(details)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OTDoctorVisitBillingResponse getById(Long visitBillingId) {
        return mapToResponse(doctorVisitBillingRepository.findById(visitBillingId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor visit billing not found")));
    }

    // ── Mapper ─────────────────────────────────────────────────────────────

    private OTDoctorVisitBillingResponse mapToResponse(OTDoctorVisitBilling v) {
        return OTDoctorVisitBillingResponse.builder()
                .id(v.getId())
                .otBillingDetailsId(v.getOtBillingDetails().getId())
                .operationExternalId(v.getOtBillingDetails().getOperationExternalId())
                .doctorExternalId(v.getDoctorExternalId())
                .doctorName(v.getDoctorName())
                .visitTime(v.getVisitTime())
                .fees(v.getFees())
                .createdAt(v.getCreatedAt())
                .updatedAt(v.getUpdatedAt())
                .build();
    }
}