package com.billing.dto;

import lombok.Data;
import java.time.LocalDate;

import com.billing.enums.PaymentStatus;

@Data
public class IpdBillRequestDTO {
    
	private Long patientExternalId;
    private Long hospitalExternalId;
    private Long admissionId;
    private LocalDate admissionDate;
    private LocalDate dischargeDate;
    private Double roomRatePerDay;
    private Double doctorFee;
    private Double medicationCharges;
    private Double nursingCharges;
    private Double diagnosticCharges;
    private Double foodCharges;
    private Long daysAdmitted;
    private Double miscellaneousCharges;
    private PaymentStatus paymentStatus;
}
