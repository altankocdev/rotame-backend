package com.altankoc.rotame.location.dto;

import lombok.Builder;

@Builder
public record LocationImageResponse(
        Long id,
        String imageUrl,
        boolean cover,
        Integer displayOrder
) {
}