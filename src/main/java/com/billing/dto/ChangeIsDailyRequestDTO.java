// ChangeIsDailyRequestDTO.java
package com.billing.dto;

import com.billing.dto.IsDaily;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeIsDailyRequestDTO {
	private Long admissionId;
    private Long serviceUsageId;  // ID of the IPDServiceUsage row
    private IsDaily isDaily;      // YES or NO
}