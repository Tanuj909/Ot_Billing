package com.billing.laboratory.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class LabTestBillItemDTO {

    private String testName;
    private Double price;
    private Double gstPercentage;
    private BigDecimal gstAmount;
}
