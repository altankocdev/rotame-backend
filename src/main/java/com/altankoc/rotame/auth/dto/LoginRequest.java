package com.altankoc.rotame.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record LoginRequest(

        @NotBlank(message = "E-posta veya kullanıcı adı boş bırakılamaz!")
        String identifier,

        @NotBlank(message = "Şifre alanı boş bırakılamaz!")
        String password
) {
}