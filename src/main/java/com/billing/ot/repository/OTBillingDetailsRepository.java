package com.billing.ot.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.billing.ot.entity.OTBillingDetails;

public interface OTBillingDetailsRepository extends JpaRepository<OTBillingDetails, Long> {
    Optional<OTBillingDetails> findByOperationExternalId(Long operationExternalId);
    Optional<OTBillingDetails> findByBillingMasterId(Long billingMasterId);
    boolean existsByOperationExternalId(Long operationExternalId);
}