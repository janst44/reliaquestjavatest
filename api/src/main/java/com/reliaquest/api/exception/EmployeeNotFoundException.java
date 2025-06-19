package com.reliaquest.api.exception;

public class EmployeeNotFoundException extends EmployeeApiException {
    public EmployeeNotFoundException(String message) {
        super(message);
    }

    public EmployeeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}