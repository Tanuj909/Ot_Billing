package com.billing.laboratory.service;

import com.billing.laboratory.dto.*;

public interface LabBillingService {

    GenerateLabBillResponse generateBill(GenerateLabBillRequest request);

    void makePayment(LabPaymentRequest request);

    LabDiscountResponse applyDiscount(LabDiscountRequest request);

    LabDiscountResponse removeDiscount(RemoveLabDiscountRequest request);

    LabBillResponse getBillByLabOrder(Long labOrderId);
}
