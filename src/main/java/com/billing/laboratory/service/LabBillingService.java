package com.billing.laboratory.service;

import com.billing.laboratory.dto.*;

public interface LabBillingService {

    GenerateLabBillResponse generateBill(GenerateLabBillRequest request);

    void makePayment(LabPaymentRequest request);

    void applyDiscount(LabDiscountRequest request);

    void removeDiscount(RemoveLabDiscountRequest request);

    LabBillResponse getBillByLabOrder(Long labOrderId);
}
