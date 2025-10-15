package com.billing.dto;

import lombok.Data;

@Data
public class PatientAdmissionRequest {
    private PatientDTO patient;
    private HospitalDTO hospital;
    private String moduleType; // e.g. "IPD"
}
