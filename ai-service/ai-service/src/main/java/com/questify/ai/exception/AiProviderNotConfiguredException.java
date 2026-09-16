package com.questify.ai.exception;

/**
 * No AI provider API key is configured.
 * AI features are unavailable until a provider is configured.
 */
public class AiProviderNotConfiguredException extends RuntimeException {

    public AiProviderNotConfiguredException(String message) {
        super(message);
    }
}