package com.billing.laboratory.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class RevenueSummaryRequest {
    private List<Long> storeIds;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}

