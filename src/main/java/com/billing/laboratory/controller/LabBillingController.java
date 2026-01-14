package com.billing.laboratory.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.billing.laboratory.dto.*;
import com.billing.laboratory.service.LabBillingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/billing/lab")
@RequiredArgsConstructor
public class LabBillingController {

    private final LabBillingService labBillingService;

    @PostMapping("/generate")
    public ResponseEntity<GenerateLabBillResponse> generateBill(
            @RequestBody GenerateLabBillRequest request) {
        return ResponseEntity.ok(labBillingService.generateBill(request));
    }

    @PostMapping("/payment")
    public ResponseEntity<Void> makePayment(
            @RequestBody LabPaymentRequest request) {
        labBillingService.makePayment(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/discount")
    public ResponseEntity<Void> applyDiscount(
            @RequestBody LabDiscountRequest request) {
        labBillingService.applyDiscount(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/discount/remove")
    public ResponseEntity<Void> removeDiscount(
            @RequestBody RemoveLabDiscountRequest request) {
        labBillingService.removeDiscount(request);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/order/{labOrderId}")
    public ResponseEntity<LabBillResponse> getBill(
            @PathVariable Long labOrderId) {
        return ResponseEntity.ok(labBillingService.getBillByLabOrder(labOrderId));
    }
}
