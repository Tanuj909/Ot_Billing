package com.billing.ot.repository;

import com.billing.ot.entity.OTBillingDetails;
import com.billing.ot.entity.OTDoctorVisitBilling;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OTDoctorVisitBillingRepository extends JpaRepository<OTDoctorVisitBilling, Long> {

    // Operation ke saare visits
    List<OTDoctorVisitBilling> findByOtBillingDetailsOrderByVisitTimeDesc(OTBillingDetails details);

    // Same doctor ka duplicate visit check (same operation mein)
    boolean existsByOtBillingDetailsAndDoctorExternalId(OTBillingDetails details, Long doctorExternalId);

    // Total fees sum for recalculate (Spring Data se)
    // Note: OTBillingDetailsService.recalculateTotals() mein use hoga
    List<OTDoctorVisitBilling> findByOtBillingDetails(OTBillingDetails details);
}