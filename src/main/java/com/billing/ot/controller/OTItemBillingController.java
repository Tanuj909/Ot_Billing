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
import com.billing.ot.dto.OTItemBillingRequest;
import com.billing.ot.dto.OTItemBillingResponse;
import com.billing.ot.dto.OTItemBillingSummaryResponse;
import com.billing.ot.dto.OTItemBillingUpdateRequest;
import com.billing.ot.enums.OTItemType;
import com.billing.ot.service.OTItemBillingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/billing/ot/items")
@RequiredArgsConstructor
public class OTItemBillingController {

    private final OTItemBillingService otItemBillingService;

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<OTItemBillingResponse>> addItem(
            @RequestBody OTItemBillingRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Item added to billing successfully",
                        otItemBillingService.addItem(request)));
    }

    @PutMapping("/{itemBillingId}/update")
    public ResponseEntity<ApiResponse<OTItemBillingResponse>> updateItem(
            @PathVariable Long itemBillingId,
            @RequestBody OTItemBillingUpdateRequest request) {

        return ResponseEntity.ok(ApiResponse.success("Item billing updated successfully",
                otItemBillingService.updateItem(itemBillingId, request)));
    }

    @DeleteMapping("/{itemBillingId}/remove")
    public ResponseEntity<ApiResponse<Void>> removeItem(
            @PathVariable Long itemBillingId) {

        otItemBillingService.removeItem(itemBillingId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from billing successfully", null));
    }

    @GetMapping("/operation/{operationId}")
    public ResponseEntity<ApiResponse<List<OTItemBillingResponse>>> getItemsByOperationId(
            @PathVariable Long operationId) {

        return ResponseEntity.ok(ApiResponse.success("Items fetched successfully",
                otItemBillingService.getItemsByOperationId(operationId)));
    }

    @GetMapping("/operation/{operationId}/type/{itemType}")
    public ResponseEntity<ApiResponse<List<OTItemBillingResponse>>> getItemsByType(
            @PathVariable Long operationId,
            @PathVariable OTItemType itemType) {

        return ResponseEntity.ok(ApiResponse.success("Items fetched successfully",
                otItemBillingService.getItemsByType(operationId, itemType)));
    }

    @GetMapping("/operation/{operationId}/summary")
    public ResponseEntity<ApiResponse<OTItemBillingSummaryResponse>> getItemSummary(
            @PathVariable Long operationId) {

        return ResponseEntity.ok(ApiResponse.success("Item summary fetched successfully",
                otItemBillingService.getItemSummary(operationId)));
    }
}