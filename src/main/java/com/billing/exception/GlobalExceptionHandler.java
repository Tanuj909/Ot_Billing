package com.billing.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BillingException.class)
    public ResponseEntity<ApiError> handleBillingException(
            BillingException ex,
            HttpServletRequest request) {

        ApiError error = new ApiError(
                ex.getMessage(),
                ex.getErrorCode().name(),
                ex.getStatus().value(),
                request.getRequestURI()
        );

        return new ResponseEntity<>(error, ex.getStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        ApiError error = new ApiError(
                "Something went wrong",
                ErrorCode.INTERNAL_SERVER_ERROR.name(),
                500,
                request.getRequestURI()
        );

        return ResponseEntity.status(500).body(error);
    }
}