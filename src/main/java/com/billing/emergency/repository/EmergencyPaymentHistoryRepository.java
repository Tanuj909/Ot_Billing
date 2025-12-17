package com.billing.emergency.repository;

import com.billing.model.EmergencyPaymentHistory;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmergencyPaymentHistoryRepository extends JpaRepository<EmergencyPaymentHistory, Long> {
	
	List<EmergencyPaymentHistory> findByEmergencyIdOrderByPaymentDateAsc(Long emergencyId);
}