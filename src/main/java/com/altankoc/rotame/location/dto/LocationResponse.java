package com.altankoc.rotame.location.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record LocationResponse(
        Long id,
        String name,
        String description,
        Double latitude,
        Double longitude,
        boolean favorite,
        List<LocationImageResponse> images,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}