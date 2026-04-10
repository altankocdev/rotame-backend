package com.altankoc.rotame.location.controller;

import com.altankoc.rotame.core.security.CustomUserDetails;
import com.altankoc.rotame.location.dto.CreateLocationRequest;
import com.altankoc.rotame.location.dto.LocationResponse;
import com.altankoc.rotame.location.dto.UpdateLocationRequest;
import com.altankoc.rotame.location.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Location", description = "Konum yönetimi işlemleri")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
@Slf4j
public class LocationController {

    private final LocationService locationService;

    @Operation(summary = "Konum ekle", description = "Yeni konum oluşturur")
    @PostMapping
    public ResponseEntity<LocationResponse> create(
            @Valid @RequestBody CreateLocationRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(locationService.create(request, userDetails.getUser().getId()));
    }

    @Operation(summary = "Konumları listele", description = "Kullanıcının tüm konumlarını sayfalı olarak getirir")
    @GetMapping
    public ResponseEntity<Page<LocationResponse>> getAll(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "false") boolean onlyFavorites,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                locationService.getAll(userDetails.getUser().getId(), onlyFavorites, pageable)
        );
    }

    @Operation(summary = "Konum getir", description = "ID'ye göre tek konum getirir")
    @GetMapping("/{id}")
    public ResponseEntity<LocationResponse> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
                locationService.getById(id, userDetails.getUser().getId())
        );
    }

    @Operation(summary = "Konum güncelle", description = "Mevcut konumu günceller")
    @PutMapping("/{id}")
    public ResponseEntity<LocationResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLocationRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
                locationService.update(id, request, userDetails.getUser().getId())
        );
    }

    @Operation(summary = "Konum sil", description = "Konumu soft delete yapar")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        locationService.delete(id, userDetails.getUser().getId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Favori toggle", description = "Konumu favoriye ekler veya favoriden çıkarır")
    @PatchMapping("/{id}/favorite")
    public ResponseEntity<LocationResponse> toggleFavorite(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
                locationService.toggleFavorite(id, userDetails.getUser().getId())
        );
    }

    @Operation(summary = "Konum geri yükle", description = "Silinmiş konumu geri yükler")
    @PatchMapping("/{id}/restore")
    public ResponseEntity<LocationResponse> restore(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
                locationService.restore(id, userDetails.getUser().getId())
        );
    }
}