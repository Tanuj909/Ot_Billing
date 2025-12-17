package com.billing.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.billing.model.OPDServiceUsage;

public interface OPDServiceUsageRepository extends JpaRepository<OPDServiceUsage, Long> {
    List<OPDServiceUsage> findByAppointmentId(Long appointmentId);
}