package com.billing.laboratory.dto;

import com.billing.enums.PaymentMode;
import lombok.Data;

@Data
public class LabPaymentRequest {

    private Long labBillingId;
    private Double amountPaid;
    private PaymentMode paymentMode;
}
