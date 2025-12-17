package com.billing.emergency.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddEmergencyItemsRequest {

    @NotNull(message = "emergencyId is required")
    private Long emergencyId;

    @NotEmpty(message = "At least one item must be provided")
    @Size(min = 1, message = "At least one item must be provided")
    private List<EmergencyItemDto> items;
}