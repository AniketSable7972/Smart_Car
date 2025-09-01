package com.smartcar.monitoring.exception;

public class AdminNotFoundException extends RuntimeException {
    
    public AdminNotFoundException(String message) {
        super(message);
    }
    
    public AdminNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
