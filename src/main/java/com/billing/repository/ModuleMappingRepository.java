package com.billing.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.billing.model.Hospital;
import com.billing.model.ModuleMapping;

public interface ModuleMappingRepository extends JpaRepository<ModuleMapping, Long> {
	
	List<ModuleMapping> findByHospitalId(Long hospitalId);
	
//	Optional<ModuleMapping> findByHospitalAndModuleType(Long externalId);
	
	//Module type ka baad mai hoga!
//	Optional<ModuleMapping> findByHospitalAndModuleType(Long externalId, String moduleType);

}
