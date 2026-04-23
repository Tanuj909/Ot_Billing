package com.billing.ot.service;

import com.billing.ot.dto.OTDoctorVisitBillingRequest;
import com.billing.ot.dto.OTDoctorVisitBillingResponse;
import com.billing.ot.dto.OTDoctorVisitBillingUpdateRequest;

import java.util.List;

public interface OTDoctorVisitBillingService {

    // Visit add karo billing mein
    OTDoctorVisitBillingResponse addDoctorVisit(OTDoctorVisitBillingRequest request);

    // Fees ya visitTime update karo
    OTDoctorVisitBillingResponse updateDoctorVisit(Long visitBillingId,
            OTDoctorVisitBillingUpdateRequest request);

    // Visit remove karo billing se
    void removeDoctorVisit(Long visitBillingId);

    // Operation ke saare visits (latest first)
    List<OTDoctorVisitBillingResponse> getByOperationId(Long operationId);

    // Single visit by id
    OTDoctorVisitBillingResponse getById(Long visitBillingId);
}