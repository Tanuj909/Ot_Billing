package com.billing.exception;

import java.time.LocalDateTime;

public class ApiError {

    private String message;
    private String errorCode;
    private int status;
    private String path;
    private LocalDateTime timestamp;

    public ApiError(String message, String errorCode, int status, String path) {
        this.message = message;
        this.errorCode = errorCode;
        this.status = status;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }

    public String getMessage() { return message; }
    public String getErrorCode() { return errorCode; }
    public int getStatus() { return status; }
    public String getPath() { return path; }
    public LocalDateTime getTimestamp() { return timestamp; }
}