package com.altankoc.rotame.user.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String username,
        String email,
        String role,
        String authProvider,
        LocalDateTime createdAt
) {
}