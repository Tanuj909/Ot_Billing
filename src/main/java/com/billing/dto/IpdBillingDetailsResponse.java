package com.billing.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class IpdBillingDetailsResponse {
    private Long id;
    private BillingMasterDTO billingMaster;
    private Long admissionId;
    private Double roomCharges;
    private Double medicationCharges;
    private Double doctorFees;
    private Double nursingCharges;
    private Double diagnosticCharges;
    private Double procedureCharges;
    private Double foodCharges;
    private Double miscellaneousCharges;
    private Long daysAdmitted;
    private Double total;

    @Data
    public static class BillingMasterDTO {
        private Long id;
        private Long hospitaExternallId;
        private Long patientExternalId;
        private Long admissionId;
        private String moduleType;
        private Double totalAmount;
        private String paymentStatus;
        private String paymentMode;
        private LocalDateTime billingDate;
    }
}