package com.billing.dto;

	// package com.hospital.billing.dto;
	import lombok.Data;

	@Data
	public class HospitalDTO {
	    // external id coming from User Management / IPD
	    private Long externalId;
	    private String name;
	    private String email;
	}

