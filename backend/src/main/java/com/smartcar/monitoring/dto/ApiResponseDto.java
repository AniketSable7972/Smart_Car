package com.smartcar.monitoring.dto;

import java.time.LocalDateTime;

public class ApiResponseDto<T> {
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    private String errorCode;
    
    // Default constructor
    public ApiResponseDto() {
        this.timestamp = LocalDateTime.now();
    }
    
    // Constructor for successful response
    public ApiResponseDto(boolean success, String message, T data) {
        this();
        this.success = success;
        this.message = message;
        this.data = data;
    }
    
    // Constructor for error response
    public ApiResponseDto(boolean success, String message, String errorCode) {
        this();
        this.success = success;
        this.message = message;
        this.errorCode = errorCode;
    }
    
    // Static factory methods for common responses
    public static <T> ApiResponseDto<T> success(String message, T data) {
        return new ApiResponseDto<>(true, message, data);
    }
    
    public static <T> ApiResponseDto<T> success(String message) {
        return new ApiResponseDto<>(true, message, null);
    }
    
    public static <T> ApiResponseDto<T> error(String message) {
        return new ApiResponseDto<>(false, message, null);
    }
    
    public static <T> ApiResponseDto<T> error(String message, String errorCode) {
        return new ApiResponseDto<>(false, message, errorCode);
    }
    
    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
}
