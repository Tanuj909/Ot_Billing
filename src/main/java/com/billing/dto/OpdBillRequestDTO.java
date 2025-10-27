package com.billing.dto;

import java.time.LocalDate;

import com.billing.enums.PaymentStatus;

import lombok.Data;

@Data
public class OpdBillRequestDTO {
    private Long patientExternalId;
    private Long hospitalExternalId;
    private LocalDate visitDate;
    private Double doctorFee;
	private Double emergencyFee;
	private Double dressing;
	private Double injection;
	private Double minorProcedure;
	private Double total;
	private PaymentStatus paymentStatus;

    
}
