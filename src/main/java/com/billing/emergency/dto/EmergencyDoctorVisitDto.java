package com.billing.emergency.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class EmergencyDoctorVisitDto {
    private String doctorName;
    private Double feesPerVisit;
    private Integer visitCount;
    private Double totalFees;
    private LocalDateTime firstVisitDate;
    private LocalDateTime lastVisitDate;
    private String remarks;
}