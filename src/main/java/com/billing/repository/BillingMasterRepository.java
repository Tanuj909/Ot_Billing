package com.billing.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.billing.model.BillingMaster;

public interface BillingMasterRepository extends JpaRepository<BillingMaster, Long> {

}
