package com.questify.ai.exception;


public class AiUpstreamException extends RuntimeException {
    public AiUpstreamException(String message, Throwable cause) {
        super(message, cause);
    }

    public AiUpstreamException(String message) {
        super(message);
    }
}
