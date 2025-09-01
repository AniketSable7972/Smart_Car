package com.smartcar.monitoring.exception;

public class CarNotFoundException extends RuntimeException {
    
    public CarNotFoundException(String message) {
        super(message);
    }
    
    public CarNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
