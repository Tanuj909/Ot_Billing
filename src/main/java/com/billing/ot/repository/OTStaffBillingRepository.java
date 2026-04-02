package com.billing.ot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.billing.ot.entity.OTBillingDetails;
import com.billing.ot.entity.OTStaffBilling;

public interface OTStaffBillingRepository extends JpaRepository<OTStaffBilling, Long> {
    List<OTStaffBilling> findByOtBillingDetails(OTBillingDetails otBillingDetails);
    List<OTStaffBilling> findByOtBillingDetailsAndStaffRole(
            OTBillingDetails otBillingDetails, String staffRole);
    boolean existsByOtBillingDetailsAndStaffExternalId(
            OTBillingDetails otBillingDetails, Long staffExternalId);
}