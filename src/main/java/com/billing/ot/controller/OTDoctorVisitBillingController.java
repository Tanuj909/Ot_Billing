package com.billing.ot.controller;

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

    /**
     * POST /api/billing/doctor-visits
     * Doctor visit fee billing mein add karo
     */
    @PostMapping
    public ResponseEntity<OTDoctorVisitBillingResponse> addDoctorVisit(
            @Valid @RequestBody OTDoctorVisitBillingRequest request) {
        return ResponseEntity.ok(doctorVisitBillingService.addDoctorVisit(request));
    }

    // ── Update ─────────────────────────────────────────────────────────────

    /**
     * PUT /api/billing/doctor-visits/{visitBillingId}
     * Fees ya visitTime update karo
     */
    @PutMapping("/{visitBillingId}")
    public ResponseEntity<OTDoctorVisitBillingResponse> updateDoctorVisit(
            @PathVariable Long visitBillingId,
            @RequestBody OTDoctorVisitBillingUpdateRequest request) {
        return ResponseEntity.ok(
                doctorVisitBillingService.updateDoctorVisit(visitBillingId, request));
    }

    // ── Remove ─────────────────────────────────────────────────────────────

    /**
     * DELETE /api/billing/doctor-visits/{visitBillingId}
     * Visit billing se hatao (billing reopen hoti hai automatically)
     */
    @DeleteMapping("/{visitBillingId}")
    public ResponseEntity<Void> removeDoctorVisit(
            @PathVariable Long visitBillingId) {
        doctorVisitBillingService.removeDoctorVisit(visitBillingId);
        return ResponseEntity.noContent().build();
    }

    // ── Get ────────────────────────────────────────────────────────────────

    /**
     * GET /api/billing/doctor-visits/{visitBillingId}
     * Single visit billing fetch
     */
    @GetMapping("/{visitBillingId}")
    public ResponseEntity<OTDoctorVisitBillingResponse> getById(
            @PathVariable Long visitBillingId) {
        return ResponseEntity.ok(doctorVisitBillingService.getById(visitBillingId));
    }

    /**
     * GET /api/billing/doctor-visits/operation/{operationId}
     * Operation ke saare doctor visit billings (latest first)
     */
    @GetMapping("/operation/{operationId}")
    public ResponseEntity<List<OTDoctorVisitBillingResponse>> getByOperationId(
            @PathVariable Long operationId) {
        return ResponseEntity.ok(doctorVisitBillingService.getByOperationId(operationId));
    }
}