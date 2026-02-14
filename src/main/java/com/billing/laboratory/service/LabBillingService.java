package com.billing.laboratory.service;

import java.util.List;

import com.billing.laboratory.dto.*;

public interface LabBillingService {

    // 1. Generate a new lab bill for a lab order
    GenerateLabBillResponse generateBill(GenerateLabBillRequest request);

    // 2. Make payment for a lab bill
    void makePayment(LabPaymentRequest request);

    // 3. Apply discount to an existing lab bill
    LabDiscountResponse applyDiscount(LabDiscountRequest request);

    // 4. Remove applied discount from a lab bill
    LabDiscountResponse removeDiscount(RemoveLabDiscountRequest request);

    // 5. Get lab bill details using lab order ID
    LabBillResponse getBillByLabOrder(Long labOrderId);

    // 6. Get laboratory revenue summary for reporting
    BillingRevenueResponseDTO getRevenueSummary(RevenueSummaryRequest request);

    // 7. Update an existing lab bill
    GenerateLabBillResponse updateBill(GenerateLabBillRequest request);

    // 8. Get payment status of a lab bill
    PaymentStatusResponse getPaymentStatus(Long labOrderId);

    // 9. Cancel a lab bill and stop further payments
    void cancelBilling(Long labOrderId);

    // 10. Initiate refund for a lab bill
    LabRefundResponse refund(LabRefundRequest request);

    // 11. Track status of a refund
    RefundStatusResponse getRefundStatus(Long refundId);

    // 12. Get refund report for a store
    RefundReportResponse getRefundReport(Long storeId);

    // 13. Get payment history by lab order ID
    List<PaymentHistoryResponse> getPaymentHistory(Long orderId);

    // 14. Get payment history by patient ID
    List<PatientPaymentHistoryResponse> getPaymentHistoryByPatient(Long patientId);

	String processLabTestRefund(Long labOrderId, Long orderItemId, String reason);
}
