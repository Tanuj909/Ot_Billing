package com.billing.dto;

import java.util.List;

import lombok.Data;

@Data
public class AddMedicationsRequest {
    private Long ipdBillingId;
    private List<MedicationItem> medications;
    
    @Data
    public static class MedicationItem {
        private String medicineName;
        private Integer quantity;
        private Double pricePerUnit;
        private String dosage;
    }
}