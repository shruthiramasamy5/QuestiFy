package com.questify.questionbank.exception;

/** A call to another microservice (e.g. ai-service) failed. */
public class UpstreamServiceException extends RuntimeException {
    public UpstreamServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
