package com.billing.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class IpdBillUpdateRequestDTO {

    private Long admissionId;
    private Long hospitalExternalId;
    private Long patientExternalId;
    private LocalDate admissionDate;
    private LocalDate dischargeDate; // today if not discharged

    // Daily fixed charges from hospital pricing
    private String roomNumber;
    private Integer bedNumber;
    private Double roomRatePerDay;
    private Double nursingChargesPerDay;
    private Double foodChargesPerDay;
    private Double diagnosticChargesPerDay;
    private Double miscChargesPerDay;

    // One-time / accumulated charges (already in DB)
    private Double miscellaneousCharges;
    private Double medicationCharges;
    private Double doctorFee;
    private Double procedureCharges;
    private Double extraServiceCharges; // extra misc one-time

    private Double discountPercentage;
    private Double gstPercentage;
}