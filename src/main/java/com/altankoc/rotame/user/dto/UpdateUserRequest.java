package com.altankoc.rotame.user.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UpdateUserRequest(

        @Size(min = 2, max = 30, message = "Ad 2 ile 30 karakter arasında olmalıdır!")
        String firstName,

        @Size(min = 2, max = 30, message = "Soyad 2 ile 30 karakter arasında olmalıdır!")
        String lastName,

        @Size(min = 3, max = 20, message = "Kullanıcı adı 3 ile 20 karakter arasında olmalıdır!")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Kullanıcı adı yalnızca harf, rakam ve alt çizgi içerebilir!")
        String username
) {
}