package com.billing.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.billing.model.Hospital;

public interface HospitalRepository extends JpaRepository<Hospital, Long>{
	Optional<Hospital> findByExternalId(Long extenalId);
}
