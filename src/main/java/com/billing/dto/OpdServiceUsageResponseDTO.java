// com.billing.dto.OpdServiceUsageResponseDTO.java
package com.billing.dto;

import lombok.Data;

@Data
public class OpdServiceUsageResponseDTO {
    private Long id;
    private String serviceName;
    private Double servicePrice;
    private Integer quantity;
    private Double totalPrice;
}