package com.billing.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class AddDoctorVisitsRequest {
    private Long ipdBillingId;
    private List<DoctorVisitItem> visits;
    
    @Data
    public static class DoctorVisitItem {
        private LocalDate visitDate;
        private String doctorName;
        private Double consultationFee;
    }
}
