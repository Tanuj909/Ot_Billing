package com.billing.ot.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.billing.ot.dto.ApiResponse;
import com.billing.ot.dto.BillingMasterResponse;
import com.billing.ot.dto.BillingMasterUpdateRequest;
import com.billing.ot.dto.OTBillingMasterRequest;
import com.billing.ot.service.BillingMasterService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/billing/master")
@RequiredArgsConstructor
public class BillingMasterController {

    private final BillingMasterService billingMasterService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<BillingMasterResponse>> createBilling(
            @RequestBody OTBillingMasterRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Billing created successfully",
                        billingMasterService.createBilling(request)));
    }

    @GetMapping("/{billingId}")
    public ResponseEntity<ApiResponse<BillingMasterResponse>> getBillingById(
            @PathVariable Long billingId) {

        return ResponseEntity.ok(ApiResponse.success("Billing fetched successfully",
                billingMasterService.getBillingById(billingId)));
    }

    @GetMapping("/operation/{operationId}")
    public ResponseEntity<ApiResponse<BillingMasterResponse>> getBillingByOperationId(
            @PathVariable Long operationId) {

        return ResponseEntity.ok(ApiResponse.success("Billing fetched successfully",
                billingMasterService.getBillingByOperationId(operationId)));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<ApiResponse<List<BillingMasterResponse>>> getBillingByPatientId(
            @PathVariable Long patientId) {

        return ResponseEntity.ok(ApiResponse.success("Billing fetched successfully",
                billingMasterService.getBillingByPatientId(patientId)));
    }

    @GetMapping("/module/{moduleType}")
    public ResponseEntity<ApiResponse<List<BillingMasterResponse>>> getBillingByModuleType(
            @PathVariable String moduleType) {

        return ResponseEntity.ok(ApiResponse.success("Billing fetched successfully",
                billingMasterService.getBillingByModuleType(moduleType)));
    }

    @PutMapping("/{billingId}/update")
    public ResponseEntity<ApiResponse<BillingMasterResponse>> updateBilling(
            @PathVariable Long billingId,
            @RequestBody BillingMasterUpdateRequest request) {

        return ResponseEntity.ok(ApiResponse.success("Billing updated successfully",
                billingMasterService.updateBilling(billingId, request)));
    }

    @PatchMapping("/{billingId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelBilling(
            @PathVariable Long billingId) {

        billingMasterService.cancelBilling(billingId);
        return ResponseEntity.ok(ApiResponse.success("Billing cancelled successfully", null));
    }
}