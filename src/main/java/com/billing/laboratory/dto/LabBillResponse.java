package com.billing.laboratory.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LabBillResponse {

    private Long labBillingId;
    private Long billingId;
    private Long labOrderId;

    private Double totalAmount;
    private Double totalPaid;
    private Double due;

    private String paymentStatus;
    private String billingStatus;

    private List<LabTestBillItemDTO> tests;
}
