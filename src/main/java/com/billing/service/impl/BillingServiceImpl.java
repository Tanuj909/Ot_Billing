package com.billing.service.impl;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import com.billing.dto.HospitalDTO;
import com.billing.dto.PatientDTO;
import com.billing.model.Hospital;
import com.billing.model.ModuleMapping;
import com.billing.model.Patient;
import com.billing.repository.HospitalRepository;
import com.billing.repository.ModuleMappingRepository;
import com.billing.repository.PatientRepository;
import com.billing.service.BillingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor //Creates Constructor for final 
public class BillingServiceImpl implements BillingService {
	
	private final HospitalRepository hospitalRepository;
	private final PatientRepository patientRepository;
	private final ModuleMappingRepository moduleMappingRepository;
	
	@Override
	
	@Transactional
	public void registerPatientAdmission(PatientDTO patientDto, HospitalDTO hospitalDto, String moduleType) {
		//Checking if the Hospital exists
		  Hospital hospital = hospitalRepository.findByExternalId(hospitalDto.getExternalId())
		            .orElseGet(() -> {
		                Hospital h = new Hospital();
		                h.setExternalId(hospitalDto.getExternalId());
		                h.setName(hospitalDto.getName());
		                h.setEmail(hospitalDto.getEmail());
		                return hospitalRepository.save(h);
		            });
		 
		 
		    // Check if patient exists
		    Patient existingPatient = patientRepository.findByExternalId(patientDto.getExternalId());
		    Patient billingPatient;
		    
		    if (existingPatient == null) {
		        Patient newPatient = new Patient();
		        newPatient.setExternalId(patientDto.getExternalId());
		        newPatient.setPatientName(patientDto.getPatientName());
		        newPatient.setHospital(hospital);
		        newPatient.setAge(patientDto.getAge());
		        newPatient.setGender(patientDto.getGender());
		        newPatient.setContactNumber(patientDto.getContactNumber());
		        billingPatient = patientRepository.save(newPatient);
		    } else {
		        billingPatient = existingPatient;
		    }
	
		    
		    //check if module Mapping Exists!
		   Optional<ModuleMapping> existingModuleMapping = moduleMappingRepository.findByHospitalIdAndModuleType(hospitalDto.getExternalId(), moduleType);
		    if(existingModuleMapping.isEmpty()) {
		    	ModuleMapping mapping = new ModuleMapping();
		    	mapping.setHospital(hospital);
                mapping.setFirstSeenAt(LocalDateTime.now());
                mapping.setModuleType(moduleType);
                mapping.setCount(0);
		    	moduleMappingRepository.save(mapping);
		    	
		    }
		    
		    //check if module Mapping Exists!
//		   Optional<ModuleMapping> existingModuleMapping = moudleMappingRepository.findByHospitalAndModuleType(hospitalDto.getExternalId(), moduleType);
//		    if(existingModuleMapping.isEmpty()) {
//		    	ModuleMapping mapping = new ModuleMapping();
//		    	mapping.setHospital(hospital);
//		    	mapping.setModuleType(moduleType);
//                mapping.setFirstSeenAt(LocalDateTime.now());
//                mapping.setCount(0);
//		    	moudleMappingRepository.save(mapping);
//		    	
//		    }
	}
}
