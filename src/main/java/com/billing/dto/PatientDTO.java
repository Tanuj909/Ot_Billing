package com.billing.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class PatientDTO {
    // external id coming from User Management / IPD
    private Long externalId;
    private String patientName;
    private String gender;
    private Integer age;
    private String contactNumber;
    private String address;
    private LocalDate admissionDate;
//    private LocalDate dischargeDate; // optional
    // optionally other fields
}
