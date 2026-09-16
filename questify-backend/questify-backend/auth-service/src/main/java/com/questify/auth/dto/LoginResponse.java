package com.questify.auth.dto;

public record LoginResponse(
        UserResponse user,
        String token
) {
}
