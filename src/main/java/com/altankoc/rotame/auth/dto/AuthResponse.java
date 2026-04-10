package com.altankoc.rotame.auth.dto;

import lombok.Builder;

@Builder
public record AuthResponse(
        Long id,
        String firstName,
        String lastName,
        String username,
        String email,
        String role,
        String accessToken,
        String refreshToken
) {
}