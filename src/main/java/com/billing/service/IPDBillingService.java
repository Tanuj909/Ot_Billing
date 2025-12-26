package com.billing.service;

import java.util.List;

import com.billing.dto.*;
import com.billing.model.*;

public interface IPDBillingService {

    // ── Core billing ─────────────────────────────────────
    IPDBillingDetails generateIpdBill(IpdBillRequestDTO request);
    IpdBillingDetailsResponse getBillingDetailsByAdmissionId(Long admissionId);
 // ADD THIS METHOD
//    IPDBillingDetails updateIpdBIll(IpdBillRequestDTO request);
    void closeBillOnDischarge(Long admissionId);
    void makePartialPayment(IpdPartialPaymentRequestDTO request);

    // ── Granular charges (services, doctor visits, medicines) ─────
    List<IPDServiceUsage> addServices(AddServicesRequest request);
    List<IPDDoctorVisit> addDoctorVisits(AddDoctorVisitsRequest request);
    List<IPDMedication> addMedications(AddMedicationsRequest request);

    List<IPDServiceUsage> getServicesByBillingId(Long ipdBillingId);
    List<IPDDoctorVisit> getDoctorVisitsByBillingId(Long ipdBillingId);
    List<IPDMedication> getMedicationsByBillingId(Long ipdBillingId);
    
 // NEW METHOD
    IPDBillingDetails updateIpdBill(IpdBillUpdateRequestDTO request);
    
//    Special Discount
    IPDBillingDetails specialDiscounts(SpecialDiscountRequestDTO request);
	IPDServiceUsage changeServiceDailyStatus(ChangeIsDailyRequestDTO request);
    
//    CreateIpdBillingAccountResponse createBillingAccount(CreateIpdBillingAccountRequest request);
}