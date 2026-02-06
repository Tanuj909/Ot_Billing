package com.billing.laboratory.controller;

import org.springframework.http.HttpStatus;
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

    /**
     * Generate Order Bill
     */
    @PostMapping("/generate")
    public ResponseEntity<GenerateLabBillResponse> generateBill(
            @RequestBody GenerateLabBillRequest request) {
        return ResponseEntity.ok(labBillingService.generateBill(request));
    }
    
    /**
     * Regenerate lab bill when order/tests are updated
     */
    @PostMapping("/regenerate")
    public ResponseEntity<GenerateLabBillResponse> regenerateBill(
            @RequestBody GenerateLabBillRequest request
    ) {
        GenerateLabBillResponse response =
                labBillingService.updateBill(request);

        return ResponseEntity.ok(response);
    }

    /**
     * Make Order Payment
     */
    @PostMapping("/payment")
    public ResponseEntity<Void> makePayment(
            @RequestBody LabPaymentRequest request) {
        labBillingService.makePayment(request);
        return ResponseEntity.ok().build();
    }

    /**
     * Apply Discount 
     */
    @PostMapping("/discount")
    public ResponseEntity<LabDiscountResponse> applyDiscount(
            @RequestBody LabDiscountRequest request) {

        LabDiscountResponse response =
                labBillingService.applyDiscount(request);

        return ResponseEntity.ok(response);
    }

    /**
     * Remove discount from the Order Bill
     */
    @PostMapping("/discount/remove")
    public ResponseEntity<LabDiscountResponse> removeDiscount(
            @RequestBody RemoveLabDiscountRequest request) {
//        labBillingService.removeDiscount(request);
        return ResponseEntity.ok(
        		labBillingService.removeDiscount(request)
        );
    }

    /**
     * Get Bill for the Order
     */
    @GetMapping("/order/{labOrderId}")
    public ResponseEntity<LabBillResponse> getBill(
            @PathVariable Long labOrderId) {
        return ResponseEntity.ok(labBillingService.getBillByLabOrder(labOrderId));
    }
    
    
    /**
     * Refund API
     */
    
    
    /**
     * Refund Status
     */
    
    /**
     * Refund Report
     */

    
    /**
     * Refund History
     */
    
    /**
     * Discount History
     */
    
    
    
    /**
     * Check Payment Status
     */
    
    /**
     * Payment History
     */
    
    /**
     * Delete/Cancel the Billing 
     */
    
    
    
   
    
    /**
     * Get Revenue for the All the Labs/Stores
     */
    @PostMapping("/revenue/summary")
    public ResponseEntity<BillingRevenueResponseDTO> getRevenueSummary(
            @RequestBody RevenueSummaryRequest request
    ) {
        return ResponseEntity.ok(
                labBillingService.getRevenueSummary(request)
        );
    }

}
