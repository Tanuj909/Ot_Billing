package com.billing.icu.controller;

import com.billing.enums.PaymentMode;
import com.billing.icu.dto.ICUBillingRequest;
import com.billing.icu.dto.ICUBillingResponse;
import com.billing.icu.dto.PaymentHistoryResponse;
import com.billing.icu.service.ICUBillingService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Billing Microservice — ICU endpoints.
 * Base path: /api/billing/icu
 */
@RestController
@RequestMapping("/api/billing/icu")
@RequiredArgsConstructor
public class ICUBillingController {

    private final ICUBillingService icuBillingService;

    /**
     * ICU microservice yahan POST karta hai — bill create ya update hoga.
     * POST /api/billing/icu/process
     */
    @PostMapping("/process")
    public ResponseEntity<ICUBillingResponse> processICUBill(
            @RequestBody ICUBillingRequest request) {
        return ResponseEntity.ok(icuBillingService.createOrUpdateBill(request));
    }

    /**
     * ICU Admission ID se bill fetch karo.
     * GET /api/billing/icu/admission/{admissionId}
     */
    @GetMapping("/admission/{admissionId}")
    public ResponseEntity<ICUBillingResponse> getByAdmission(
            @PathVariable Long admissionId) {
        return ResponseEntity.ok(icuBillingService.getBillByAdmission(admissionId));
    }

    /**
     * ICUBillingDetails ID se bill fetch karo.
     * GET /api/billing/icu/{billingId}
     */
    @GetMapping("/{billingId}")
    public ResponseEntity<ICUBillingResponse> getById(
            @PathVariable Long billingId) {
        return ResponseEntity.ok(icuBillingService.getBillById(billingId));
    }

	// ─────────────────────────────────────────────────────────────
	// GET TOTAL BILL AMOUNT
	// ─────────────────────────────────────────────────────────────
	/**
	 * ICU Admission ID se sirf total billing amount fetch karo.
	 *
	 * GET /api/billing/icu/total-amount/{admissionId}
	 */
	@GetMapping("/total-amount/{admissionId}")
	public ResponseEntity<Double> getTotalAmount(@PathVariable Long admissionId) {

		return ResponseEntity.ok(icuBillingService.getTotalAmount(admissionId));
	}
	
    // ─────────────────────────────────────────────────────────────
    //  GET PAYMENT HISTORY
    // ─────────────────────────────────────────────────────────────
    /**
     * ICU Admission ID se saari payment history fetch karo.
     *
     * GET /api/billing/icu/payment-history/{admissionId}
     */
    @GetMapping("/payment-history/{admissionId}")
    public ResponseEntity<List<PaymentHistoryResponse>> getPaymentHistory(
            @PathVariable Long admissionId) {

        return ResponseEntity.ok(
                icuBillingService.getPaymentHistory(admissionId)
        );
    }
	
    /**
     * Payment record karo.
     * POST /api/billing/icu/payment?admissionId=&amount=&paymentMode=&referenceNumber=&receivedBy=
     */
    @PostMapping("/payment")
    public ResponseEntity<ICUBillingResponse> addPayment(
            @RequestParam Long admissionId,
            @RequestParam Double amount,
            @RequestParam PaymentMode paymentMode,
            @RequestParam(required = false) String referenceNumber,
            @RequestParam(required = false) String receivedBy) {

        return ResponseEntity.ok(
                icuBillingService.addPayment(admissionId, amount, paymentMode, referenceNumber, receivedBy)
        );
    }

    /**
     * Bill close karo (discharge ke baad ICU call karega).
     * POST /api/billing/icu/close/{admissionId}
     */
    @PostMapping("/close/{admissionId}")
    public ResponseEntity<ICUBillingResponse> closeBill(
            @PathVariable Long admissionId) {
        return ResponseEntity.ok(icuBillingService.closeBill(admissionId));
    }
    
    // ─────────────────────────────────────────────────────────────
    // UPDATE BILLING STATUS
    // ─────────────────────────────────────────────────────────────

   @PutMapping("/status/update")
   public ResponseEntity<ICUBillingResponse> updateBillingStatus(
           @RequestParam Long admissionId,
           @RequestParam String status) {

       return ResponseEntity.ok(
               icuBillingService.updateBillingStatus(admissionId, status)
       );
   }
    
}