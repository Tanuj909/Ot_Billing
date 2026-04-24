package com.billing.ot.controller;

import com.billing.dto.BillingApiResponse;
import com.billing.ot.dto.OTDoctorVisitBillingRequest;
import com.billing.ot.dto.OTDoctorVisitBillingResponse;
import com.billing.ot.dto.OTDoctorVisitBillingUpdateRequest;
import com.billing.ot.service.OTDoctorVisitBillingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/billing/doctor-visits")
@RequiredArgsConstructor
public class OTDoctorVisitBillingController {

    private final OTDoctorVisitBillingService doctorVisitBillingService;

    // ── Add ────────────────────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<BillingApiResponse<OTDoctorVisitBillingResponse>> addDoctorVisit(
            @Valid @RequestBody OTDoctorVisitBillingRequest request) {

        OTDoctorVisitBillingResponse response =
                doctorVisitBillingService.addDoctorVisit(request);

        return ResponseEntity.ok(
                BillingApiResponse.<OTDoctorVisitBillingResponse>builder()
                        .success(true)
                        .message("Doctor visit billing created successfully")
                        .data(response)
                        .build()
        );
    }

    // ── Update ─────────────────────────────────────────────────────────────
    @PutMapping("/{visitBillingId}")
    public ResponseEntity<BillingApiResponse<OTDoctorVisitBillingResponse>> updateDoctorVisit(
            @PathVariable Long visitBillingId,
            @RequestBody OTDoctorVisitBillingUpdateRequest request) {

        OTDoctorVisitBillingResponse response =
                doctorVisitBillingService.updateDoctorVisit(visitBillingId, request);

        return ResponseEntity.ok(
                BillingApiResponse.<OTDoctorVisitBillingResponse>builder()
                        .success(true)
                        .message("Doctor visit billing updated successfully")
                        .data(response)
                        .build()
        );
    }

    // ── Remove ─────────────────────────────────────────────────────────────
    @DeleteMapping("/{visitBillingId}")
    public ResponseEntity<BillingApiResponse<Void>> removeDoctorVisit(
            @PathVariable Long visitBillingId) {

        doctorVisitBillingService.removeDoctorVisit(visitBillingId);

        return ResponseEntity.ok(
                BillingApiResponse.<Void>builder()
                        .success(true)
                        .message("Doctor visit billing deleted successfully")
                        .data(null)
                        .build()
        );
    }

    // ── Get By ID ──────────────────────────────────────────────────────────
    @GetMapping("/{visitBillingId}")
    public ResponseEntity<BillingApiResponse<OTDoctorVisitBillingResponse>> getById(
            @PathVariable Long visitBillingId) {

        OTDoctorVisitBillingResponse response =
                doctorVisitBillingService.getById(visitBillingId);

        return ResponseEntity.ok(
                BillingApiResponse.<OTDoctorVisitBillingResponse>builder()
                        .success(true)
                        .message("Doctor visit billing fetched successfully")
                        .data(response)
                        .build()
        );
    }

    // ── Get By Operation ───────────────────────────────────────────────────
    @GetMapping("/operation/{operationId}")
    public ResponseEntity<BillingApiResponse<List<OTDoctorVisitBillingResponse>>> getByOperationId(
            @PathVariable Long operationId) {

        List<OTDoctorVisitBillingResponse> response =
                doctorVisitBillingService.getByOperationId(operationId);

        return ResponseEntity.ok(
                BillingApiResponse.<List<OTDoctorVisitBillingResponse>>builder()
                        .success(true)
                        .message("Doctor visit billing list fetched successfully")
                        .data(response)
                        .build()
        );
    }
}