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
import com.billing.ot.dto.OTAdvancePaymentRequest;
import com.billing.ot.dto.OTPaymentHistoryResponse;
import com.billing.ot.dto.OTPaymentRequest;
import com.billing.ot.dto.OTPaymentResponse;
import com.billing.ot.dto.OTRefundRequest;
import com.billing.ot.dto.OTRefundResponse;
import com.billing.ot.service.OTPaymentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/billing/ot/payment")
@RequiredArgsConstructor
public class OTPaymentController {

    private final OTPaymentService otPaymentService;

    @PostMapping("/advance")
    public ResponseEntity<ApiResponse<OTPaymentResponse>> makeAdvancePayment(
            @RequestBody OTAdvancePaymentRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Advance payment successful",
                        otPaymentService.makeAdvancePayment(request)));
    }
    
    @PostMapping("/make")
    public ResponseEntity<ApiResponse<OTPaymentResponse>> makePayment(
            @RequestBody OTPaymentRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment made successfully",
                        otPaymentService.makePayment(request)));
    }

    @PostMapping("/refund/initiate")
    public ResponseEntity<ApiResponse<OTRefundResponse>> initiateRefund(
            @RequestBody OTRefundRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Refund initiated successfully",
                        otPaymentService.initiateRefund(request)));
    }

    @PatchMapping("/refund/{refundId}/complete")
    public ResponseEntity<ApiResponse<OTRefundResponse>> completeRefund(
            @PathVariable Long refundId) {

        return ResponseEntity.ok(ApiResponse.success("Refund completed successfully",
                otPaymentService.completeRefund(refundId)));
    }

    @GetMapping("/operation/{operationId}/history")
    public ResponseEntity<ApiResponse<OTPaymentHistoryResponse>> getPaymentHistory(
            @PathVariable Long operationId) {

        return ResponseEntity.ok(ApiResponse.success("Payment history fetched successfully",
                otPaymentService.getPaymentHistory(operationId)));
    }
}