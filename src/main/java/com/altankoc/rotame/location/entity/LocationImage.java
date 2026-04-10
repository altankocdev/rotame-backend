package com.altankoc.rotame.location.entity;

import com.altankoc.rotame.core.base.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Table(
        name = "location_images",
        indexes = {
                @Index(columnList = "location_id", name = "idx_location_image_location_id")
        }
)
@Entity
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LocationImage extends BaseEntity {

    @NotBlank(message = "Resim URL'i boş bırakılamaz!")
    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Builder.Default
    @Column(name = "is_cover", nullable = false)
    private boolean cover = false;

    @Column(name = "display_order")
    private Integer displayOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;
}