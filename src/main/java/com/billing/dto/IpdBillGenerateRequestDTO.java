package com.billing.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class IpdBillGenerateRequestDTO {
    private Long admissionId;
    private Long patientExternalId;
    private Long hospitalExternalId;
    private LocalDate admissionDate;
    private LocalDate dischargeDate;

    // Per-day fixed rates
    private Double roomRatePerDay;
    private Double nursingChargesPerDay;
    private Double foodChargesPerDay;

    // Dynamic data
    private List<DoctorVisitDTO> doctorVisits;
    private List<ServiceProvidedDTO> services;
    private List<MedicationDispensedDTO> medications;

    // Configurable
    private Double discountPercentage;
    private Double gstPercentage;

    // Nested DTOs
    @Getter @Setter
    public static class DoctorVisitDTO {
        private Long doctorId;
        private Double fee;
        private LocalDateTime visitDate;
    }

    @Getter @Setter
    public static class ServiceProvidedDTO {
        private String serviceName;
        private Double cost;
        private LocalDateTime providedDate;
    }

    @Getter @Setter
    public static class MedicationDispensedDTO {
        private String medicineName;
        private Integer quantity;
        private Double unitPrice;
        private Double totalCost;
    }
}