package com.billing.laboratory.dto;

import java.time.LocalDate;
import lombok.Data;

@Data
public class RefundReportRequest {

//    private LocalDate fromDate;
//    private LocalDate toDate;

//    private Long labOrderId;   // optional
    private Long storeId;      // optional
}
