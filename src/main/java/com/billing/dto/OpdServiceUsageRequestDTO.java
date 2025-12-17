package com.billing.dto;

public record OpdServiceUsageRequestDTO(
		 Long appointmentId,
		 Long serviceId,
		 String serviceName,
		 Double servicePrice,
		 Integer quantity
		) {}
