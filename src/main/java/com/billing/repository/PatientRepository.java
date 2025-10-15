package com.billing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.billing.model.Patient;

public interface PatientRepository extends JpaRepository<Patient, Long>{
	Patient findByExternalId(Long externalId);
}
