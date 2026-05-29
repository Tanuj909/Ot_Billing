package com.billing.icu.service;

import com.billing.icu.dto.ICUBillingRequest;
import com.billing.icu.dto.ICUBillingResponse;
import com.billing.icu.dto.PaymentHistoryResponse;

import java.util.List;

import com.billing.enums.PaymentMode;

public interface ICUBillingService {

    /**
     * ICU se data receive karke bill create ya update karo.
     * Agar icuAdmissionId ke liye bill already exist karta hai → recalculate.
     * Nahi karta → fresh bill create karo with BillingMaster.
     */
    ICUBillingResponse createOrUpdateBill(ICUBillingRequest request);

    /**
     * Admission ID se bill fetch karo.
     */
    ICUBillingResponse getBillByAdmission(Long icuAdmissionId);

    /**
     * BillingDetails ID se bill fetch karo.
     */
    ICUBillingResponse getBillById(Long billingDetailsId);
    
    /**
     * get Total Amount
     */
    Double getTotalAmount(Long admissionId);

    /**
     * Payment record karo aur due update karo.
     */
    ICUBillingResponse addPayment(Long icuAdmissionId, Double amount,
                                   PaymentMode paymentMode, String referenceNumber,
                                   String receivedBy);

    /**
     * Bill band karo (discharge ke baad).
     */
    ICUBillingResponse closeBill(Long icuAdmissionId);

	List<PaymentHistoryResponse> getPaymentHistory(Long admissionId);

//	Update Billing Status
	ICUBillingResponse updateBillingStatus(Long admissionId, String status);
}