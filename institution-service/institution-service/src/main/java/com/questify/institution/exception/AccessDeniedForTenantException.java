package com.questify.institution.exception;

public class AccessDeniedForTenantException extends RuntimeException {

    public AccessDeniedForTenantException(String message) {
        super(message);
    }
}
