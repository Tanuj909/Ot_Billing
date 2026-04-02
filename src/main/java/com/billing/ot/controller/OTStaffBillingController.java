package com.billing.ot.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.billing.ot.dto.ApiResponse;
import com.billing.ot.dto.OTStaffBillingRequest;
import com.billing.ot.dto.OTStaffBillingResponse;
import com.billing.ot.dto.OTStaffBillingUpdateRequest;
import com.billing.ot.service.OTStaffBillingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/billing/ot/staff")
@RequiredArgsConstructor
public class OTStaffBillingController {

    private final OTStaffBillingService otStaffBillingService;

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<OTStaffBillingResponse>> addStaffBilling(
            @RequestBody OTStaffBillingRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Staff billing added successfully",
                        otStaffBillingService.addStaffBilling(request)));
    }

    @GetMapping("/operation/{operationId}")
    public ResponseEntity<ApiResponse<List<OTStaffBillingResponse>>> getStaffBillingByOperationId(
            @PathVariable Long operationId) {

        return ResponseEntity.ok(ApiResponse.success("Staff billing fetched successfully",
                otStaffBillingService.getStaffBillingByOperationId(operationId)));
    }

    @GetMapping("/{staffBillingId}")
    public ResponseEntity<ApiResponse<OTStaffBillingResponse>> getStaffBillingById(
            @PathVariable Long staffBillingId) {

        return ResponseEntity.ok(ApiResponse.success("Staff billing fetched successfully",
                otStaffBillingService.getStaffBillingById(staffBillingId)));
    }

    @PutMapping("/{staffBillingId}/update")
    public ResponseEntity<ApiResponse<OTStaffBillingResponse>> updateStaffBilling(
            @PathVariable Long staffBillingId,
            @RequestBody OTStaffBillingUpdateRequest request) {

        return ResponseEntity.ok(ApiResponse.success("Staff billing updated successfully",
                otStaffBillingService.updateStaffBilling(staffBillingId, request)));
    }

    @DeleteMapping("/{staffBillingId}/remove")
    public ResponseEntity<ApiResponse<Void>> removeStaffBilling(
            @PathVariable Long staffBillingId) {

        otStaffBillingService.removeStaffBilling(staffBillingId);
        return ResponseEntity.ok(ApiResponse.success("Staff billing removed successfully", null));
    }
}
