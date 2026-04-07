package com.billing.ot.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.billing.ot.dto.ApiResponse;
import com.billing.ot.dto.OTBillingDetailsRequest;
import com.billing.ot.dto.OTBillingDetailsResponse;
import com.billing.ot.dto.OTBillingSummaryResponse;
import com.billing.ot.service.OTBillingDetailsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/billing/ot")
@RequiredArgsConstructor
public class OTBillingDetailsController {

    private final OTBillingDetailsService otBillingDetailsService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<OTBillingDetailsResponse>> createOTBillingDetails(
            @RequestBody OTBillingDetailsRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("OT Billing created successfully",
                        otBillingDetailsService.createOTBillingDetails(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OTBillingDetailsResponse>> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(ApiResponse.success("OT Billing fetched successfully",
                otBillingDetailsService.getById(id)));
    }

    @GetMapping("/operation/{operationId}")
    public ResponseEntity<ApiResponse<OTBillingDetailsResponse>> getByOperationId(
            @PathVariable Long operationId) {

        return ResponseEntity.ok(ApiResponse.success("OT Billing fetched successfully",
                otBillingDetailsService.getByOperationId(operationId)));
    }

    @PatchMapping("/operation/{operationId}/recalculate")
    public ResponseEntity<ApiResponse<OTBillingDetailsResponse>> recalculateTotals(
            @PathVariable Long operationId) {

        return ResponseEntity.ok(ApiResponse.success("Totals recalculated successfully",
                otBillingDetailsService.recalculateTotals(operationId)));
    }

    @PostMapping("/operation/{operationId}/close")
    public ResponseEntity<ApiResponse<OTBillingDetailsResponse>> closeBilling(
            @PathVariable Long operationId) {

        return ResponseEntity.ok(ApiResponse.success("Billing closed successfully",
                otBillingDetailsService.closeBilling(operationId)));
    }
    
    @GetMapping("/operation/{operationId}/summary")
    public ResponseEntity<ApiResponse<OTBillingSummaryResponse>> getBillingSummary(
            @PathVariable Long operationId) {

        return ResponseEntity.ok(ApiResponse.success("Billing summary fetched successfully",
                otBillingDetailsService.getBillingSummary(operationId)));
    }
}