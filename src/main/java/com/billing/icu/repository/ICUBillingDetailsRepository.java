package com.billing.icu.repository;

import com.billing.icu.entity.ICUBillingDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ICUBillingDetailsRepository extends JpaRepository<ICUBillingDetails, Long> {

    Optional<ICUBillingDetails> findByIcuAdmissionId(Long icuAdmissionId);

    List<ICUBillingDetails> findByHospitalExternalId(Long hospitalId);

    List<ICUBillingDetails> findByHospitalExternalIdAndBillingStatus(Long hospitalId, String billingStatus);

    List<ICUBillingDetails> findByPatientExternalId(Long patientId);
}