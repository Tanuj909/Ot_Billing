package com.billing.laboratory.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RefundReportResponse {

    private Double totalRefundAmount;
    private Integer totalRefundCount;

    private List<RefundReportItemDTO> refunds;
}
