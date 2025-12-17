package com.billing.emergency.dto;

import lombok.Data;

@Data
public class AddDoctorVisitResponse {
    private Long emergencyId;
    private String doctorName;
    private Double feesPerVisit;
    private Integer totalVisitsForDoctor;
    private Double totalFeesThisDoctor;
    private Double grandTotalDoctorFees;
    private Double updatedFinalBill;
    private Double updatedDue;
    private String message = "Doctor visit(s) recorded successfully";
}