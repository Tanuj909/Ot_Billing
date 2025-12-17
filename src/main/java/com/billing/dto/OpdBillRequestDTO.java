package com.billing.dto;

import java.time.LocalDateTime;

import com.billing.enums.PaymentMode;
import com.billing.enums.PaymentStatus;
import lombok.Data;

@Data
public class OpdBillRequestDTO {
    private Long patientExternalId;
    private Long hospitalExternalId;
    private Long appointmentId;
    private Long doctorId;
    private LocalDateTime visitDate;
    private Double doctorFee;
	private Double emergencyFee;
	private Double dressing;
	private Double injection;
	private Double minorProcedure;
	private Double total;
	private Double serviceCharges;
	private PaymentStatus paymentStatus;
	private PaymentMode paymentMode;
}
