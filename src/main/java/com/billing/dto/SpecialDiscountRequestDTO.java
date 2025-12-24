package com.billing.dto;

import lombok.Data;

@Data
public class SpecialDiscountRequestDTO {
	
	private Long admissionId;
	private Double specialDiscountPercentage;
	private String reason;
	private String message = "Special Discount Applied!";
}
