package com.altankoc.rotame.location.service;

import com.altankoc.rotame.core.exception.BusinessException;
import com.altankoc.rotame.core.exception.ResourceNotFoundException;
import com.altankoc.rotame.core.exception.UnauthorizedException;
import com.altankoc.rotame.core.s3.S3Service;
import com.altankoc.rotame.location.dto.LocationImageResponse;
import com.altankoc.rotame.location.entity.Location;
import com.altankoc.rotame.location.entity.LocationImage;
import com.altankoc.rotame.location.mapper.LocationImageMapper;
import com.altankoc.rotame.location.repository.LocationImageRepository;
import com.altankoc.rotame.location.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationImageServiceImpl implements LocationImageService {

    private final LocationRepository locationRepository;
    private final LocationImageRepository locationImageRepository;
    private final LocationImageMapper locationImageMapper;
    private final S3Service s3Service;

    private static final int MAX_IMAGES = 4;
    private static final List<String> ALLOWED_TYPES = List.of(
            "image/jpeg", "image/png", "image/webp"
    );

    @Override
    @Transactional
    public LocationImageResponse upload(Long locationId, Long userId, MultipartFile file) {
        Location location = getLocationAndCheckOwnership(locationId, userId);

        int imageCount = locationImageRepository.countByLocationIdAndDeletedFalse(locationId);
        if (imageCount >= MAX_IMAGES) {
            throw new BusinessException("Bir konuma en fazla " + MAX_IMAGES + " resim eklenebilir!");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new BusinessException("Sadece JPEG, PNG ve WebP formatları desteklenmektedir!");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BusinessException("Dosya boyutu 5MB'dan büyük olamaz!");
        }

        try {
            String fileName = String.format("locations/%d/%s-%s",
                    locationId,
                    UUID.randomUUID(),
                    file.getOriginalFilename()
            );

            String imageUrl = s3Service.uploadFile(fileName, file.getBytes(), contentType);

            boolean isCover = imageCount == 0;

            int displayOrder = imageCount + 1;

            LocationImage locationImage = LocationImage.builder()
                    .imageUrl(imageUrl)
                    .cover(isCover)
                    .displayOrder(displayOrder)
                    .location(location)
                    .build();

            locationImageRepository.save(locationImage);
            log.info("Image uploaded for location: {}", locationId);

            return locationImageMapper.toResponse(locationImage);

        } catch (IOException e) {
            log.error("Failed to upload image: {}", e.getMessage());
            throw new BusinessException("Resim yüklenirken hata oluştu!");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationImageResponse> getAll(Long locationId, Long userId) {
        getLocationAndCheckOwnership(locationId, userId);

        return locationImageRepository.findByLocationIdAndDeletedFalse(locationId)
                .stream()
                .map(locationImageMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long locationId, Long imageId, Long userId) {
        getLocationAndCheckOwnership(locationId, userId);

        LocationImage image = locationImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Resim bulunamadı!"));

        String fileName = extractFileNameFromUrl(image.getImageUrl());
        s3Service.deleteFile(fileName);

        image.softDelete();
        locationImageRepository.save(image);

        log.info("Image deleted: {}", imageId);
    }

    @Override
    @Transactional
    public LocationImageResponse setCover(Long locationId, Long imageId, Long userId) {
        getLocationAndCheckOwnership(locationId, userId);

        locationImageRepository.findByLocationIdAndDeletedFalse(locationId)
                .forEach(img -> {
                    if (img.isCover()) {
                        img.setCover(false);
                        locationImageRepository.save(img);
                    }
                });

        LocationImage image = locationImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Resim bulunamadı!"));

        image.setCover(true);
        locationImageRepository.save(image);

        log.info("Cover image set: {} for location: {}", imageId, locationId);
        return locationImageMapper.toResponse(image);
    }

    private Location getLocationAndCheckOwnership(Long locationId, Long userId) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Konum bulunamadı!"));

        if (location.isDeleted()) {
            throw new ResourceNotFoundException("Konum bulunamadı!");
        }

        if (!location.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Bu konuma erişim yetkiniz yok!");
        }

        return location;
    }

    private String extractFileNameFromUrl(String imageUrl) {
        return imageUrl.substring(imageUrl.indexOf("locations/"));
    }
}