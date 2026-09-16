package com.questify.ai.exception;

/**
 * The model responded, but its output failed validation against the
 * original request (e.g. it invented a question id that was never in the
 * candidate pool, or returned the wrong count). Never silently accepted.
 */
public class AiResponseValidationException extends RuntimeException {
    public AiResponseValidationException(String message) {
        super(message);
    }
}
