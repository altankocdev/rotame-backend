package com.altankoc.rotame.user.controller;

import com.altankoc.rotame.core.security.CustomUserDetails;
import com.altankoc.rotame.user.dto.UpdateUserRequest;
import com.altankoc.rotame.user.dto.UserResponse;
import com.altankoc.rotame.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "Kullanıcı profil işlemleri")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @Operation(summary = "Profili getir", description = "Giriş yapmış kullanıcının profilini getirir")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
                userService.getMe(userDetails.getUser().getId())
        );
    }

    @Operation(summary = "Profili güncelle", description = "Kullanıcı adı, ad ve soyad günceller")
    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateMe(
            @Valid @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
                userService.updateMe(userDetails.getUser().getId(), request)
        );
    }

    @Operation(summary = "Hesabı sil", description = "Hesabı soft delete yapar")
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMe(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        userService.deleteMe(userDetails.getUser().getId());
        return ResponseEntity.noContent().build();
    }
}