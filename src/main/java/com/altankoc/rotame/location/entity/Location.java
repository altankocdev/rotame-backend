package com.altankoc.rotame.location.entity;

import com.altankoc.rotame.core.base.BaseEntity;
import com.altankoc.rotame.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Table(
        name = "locations",
        indexes = {
                @Index(columnList = "user_id", name = "idx_location_user_id"),
                @Index(columnList = "latitude, longitude", name = "idx_location_coordinates")
        }
)
@Entity
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Location extends BaseEntity {

    @NotBlank(message = "Konum adı boş bırakılamaz!")
    @Size(min = 1, max = 100, message = "Konum adı 1 ile 100 karakter arasında olmalıdır!")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Size(max = 500, message = "Açıklama 500 karakteri geçemez!")
    @Column(name = "description", length = 500)
    private String description;

    @NotNull(message = "Enlem boş bırakılamaz!")
    @DecimalMin(value = "-90.0", message = "Enlem -90 ile 90 arasında olmalıdır!")
    @DecimalMax(value = "90.0", message = "Enlem -90 ile 90 arasında olmalıdır!")
    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @NotNull(message = "Boylam boş bırakılamaz!")
    @DecimalMin(value = "-180.0", message = "Boylam -180 ile 180 arasında olmalıdır!")
    @DecimalMax(value = "180.0", message = "Boylam -180 ile 180 arasında olmalıdır!")
    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Builder.Default
    @Column(name = "is_favorite", nullable = false)
    private boolean favorite = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<LocationImage> locationImages = new ArrayList<>();
}
