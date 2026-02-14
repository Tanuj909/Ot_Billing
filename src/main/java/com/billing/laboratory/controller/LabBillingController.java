package com.billing.laboratory.controller;

import java.util.List;

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
    @PostMapping("/refund")
    public ResponseEntity<LabRefundResponse> refund(
            @RequestBody LabRefundRequest request
    ) {
        return ResponseEntity.ok(
                labBillingService.refund(request)
        );
    }

    /**
     * Refund Status
     * Accessible by: RECEPTIONIST, PATIENT, ADMIN
     */
    @GetMapping("/refund/status/{orderId}")
    public ResponseEntity<RefundStatusResponse> getRefundStatus(
            @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(
                labBillingService.getRefundStatus(orderId)
        );
    }
    
    /**
     * Refund Report
     * Accessible by: ADMIN, RECEPTIONIST
     */
    @PostMapping("/refund/report")
//    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public ResponseEntity<RefundReportResponse> getRefundReport(
            @RequestParam Long storeId
    ) {
        return ResponseEntity.ok(
                labBillingService.getRefundReport(storeId)
        );
    }
    
    @PutMapping("/refund/order-item")
    public ResponseEntity<?> processLabTestRefund(
            @RequestBody LabTestRefundRequestDTO request) {

        try {

            String response = labBillingService.processLabTestRefund(
                    request.getLabOrderId(),
                    request.getOrderItemId(),
                    request.getReason()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            e.printStackTrace();   // 🔥 SHOW REAL ERROR

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());   // return real message
        }
    }

    
    /**
     * Check Payment Status
     */
    @GetMapping("/payment/status/{labOrderId}")
    public ResponseEntity<PaymentStatusResponse> getPaymentStatus(
            @PathVariable Long labOrderId
    ) {
        return ResponseEntity.ok(
                labBillingService.getPaymentStatus(labOrderId)
        );
    }
    
    /**
     * Payment History
     */
    @GetMapping("/payment/history/{orderId}")
    public ResponseEntity<List<PaymentHistoryResponse>> getPaymentHistory(
    		@PathVariable Long orderId
    ){
    	return ResponseEntity.ok(labBillingService.getPaymentHistory(orderId));
    }
    
    /**
     * Patient Payment History(By Patient Id)
     */
    @GetMapping("/payment/history/patient/{patientId}")
    public ResponseEntity<List<PatientPaymentHistoryResponse>> getPaymentHistoryByPatient(
    		@PathVariable Long patientId
    ){
    	return ResponseEntity.ok(labBillingService.getPaymentHistoryByPatient(patientId));
    }
    
    /**
     * Delete / Cancel the Billing
     */
    @PostMapping("/cancel/{labOrderId}")
    public ResponseEntity<Void> cancelBilling(
            @PathVariable Long labOrderId
    ) {
        labBillingService.cancelBilling(labOrderId);
        return ResponseEntity.ok().build();
    }

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
