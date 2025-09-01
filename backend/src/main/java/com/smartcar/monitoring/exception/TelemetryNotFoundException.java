package com.smartcar.monitoring.exception;

public class TelemetryNotFoundException extends RuntimeException {
    
    public TelemetryNotFoundException(String message) {
        super(message);
    }
    
    public TelemetryNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
