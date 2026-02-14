package com.billing.laboratory.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LabTestBillItemDTO {

	private Long id;
	private Long orderItemId;
    private String testName;
    private Double price;
    private Double totalAmount;
    private Double gstPercentage;
    private BigDecimal gstAmount;
}
