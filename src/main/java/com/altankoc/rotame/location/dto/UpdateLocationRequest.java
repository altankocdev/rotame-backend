package com.altankoc.rotame.location.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UpdateLocationRequest(

        @Size(min = 1, max = 100, message = "Konum adı 1 ile 100 karakter arasında olmalıdır!")
        String name,

        @Size(max = 500, message = "Açıklama 500 karakteri geçemez!")
        String description,

        @DecimalMin(value = "-90.0", message = "Enlem -90 ile 90 arasında olmalıdır!")
        @DecimalMax(value = "90.0", message = "Enlem -90 ile 90 arasında olmalıdır!")
        Double latitude,

        @DecimalMin(value = "-180.0", message = "Boylam -180 ile 180 arasında olmalıdır!")
        @DecimalMax(value = "180.0", message = "Boylam -180 ile 180 arasında olmalıdır!")
        Double longitude
) {
}