package com.billing.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.billing.model.IPDServiceUsage;


public interface IPDServiceUsageRepository extends JpaRepository<IPDServiceUsage, Long> {
    List<IPDServiceUsage> findByIpdBillingDetailsId(Long ipdBillingId);

//	List<IPDServiceUsage> findbyIpdBillingDetails(Long id);
}