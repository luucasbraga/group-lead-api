package com.grouplead.exception;

public class IntegrationException extends RuntimeException {

    private final String service;

    public IntegrationException(String service, String message) {
        super(message);
        this.service = service;
    }

    public IntegrationException(String service, String message, Throwable cause) {
        super(message, cause);
        this.service = service;
    }

    public String getService() {
        return service;
    }
}
