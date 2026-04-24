package com.billing.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BillingApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
}