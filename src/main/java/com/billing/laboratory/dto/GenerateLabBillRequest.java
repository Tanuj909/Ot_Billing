package com.billing.laboratory.dto;

import java.util.List;
import lombok.Data;

@Data
public class GenerateLabBillRequest {

    private Long hospitaExternallId;
    private Long storeId;
    private Long patientExternalId;
    private Long labOrderId;

    private List<LabTestBillItemDTO> tests;
}
