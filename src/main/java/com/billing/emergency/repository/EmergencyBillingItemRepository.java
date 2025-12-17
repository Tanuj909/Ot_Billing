package com.billing.emergency.repository;

import com.billing.model.EmergencyBillingItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmergencyBillingItemRepository extends JpaRepository<EmergencyBillingItem, Long> {
}
