package com.altankoc.rotame.location.controller;

import com.altankoc.rotame.core.security.CustomUserDetails;
import com.altankoc.rotame.location.dto.LocationImageResponse;
import com.altankoc.rotame.location.service.LocationImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Location Image", description = "Konum resim yönetimi işlemleri")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/v1/locations/{locationId}/images")
@RequiredArgsConstructor
@Slf4j
public class LocationImageController {

    private final LocationImageService locationImageService;

    @Operation(summary = "Resim yükle", description = "Konuma resim yükler (max 4 resim, max 5MB)")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LocationImageResponse> upload(
            @PathVariable Long locationId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(locationImageService.upload(locationId, userDetails.getUser().getId(), file));
    }

    @Operation(summary = "Resimleri listele", description = "Konumun tüm resimlerini getirir")
    @GetMapping
    public ResponseEntity<List<LocationImageResponse>> getAll(
            @PathVariable Long locationId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
                locationImageService.getAll(locationId, userDetails.getUser().getId())
        );
    }

    @Operation(summary = "Resim sil", description = "Resmi siler")
    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long locationId,
            @PathVariable Long imageId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        locationImageService.delete(locationId, imageId, userDetails.getUser().getId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Kapak resmi yap", description = "Resmi kapak resmi olarak ayarlar")
    @PatchMapping("/{imageId}/cover")
    public ResponseEntity<LocationImageResponse> setCover(
            @PathVariable Long locationId,
            @PathVariable Long imageId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
                locationImageService.setCover(locationId, imageId, userDetails.getUser().getId())
        );
    }
}