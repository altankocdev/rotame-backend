package com.altankoc.rotame.location.service;

import com.altankoc.rotame.core.exception.BusinessException;
import com.altankoc.rotame.core.exception.ResourceNotFoundException;
import com.altankoc.rotame.core.exception.UnauthorizedException;
import com.altankoc.rotame.location.dto.CreateLocationRequest;
import com.altankoc.rotame.location.dto.LocationResponse;
import com.altankoc.rotame.location.dto.UpdateLocationRequest;
import com.altankoc.rotame.location.entity.Location;
import com.altankoc.rotame.location.mapper.LocationMapper;
import com.altankoc.rotame.location.repository.LocationRepository;
import com.altankoc.rotame.user.entity.User;
import com.altankoc.rotame.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final LocationMapper locationMapper;

    @Override
    @Transactional
    public LocationResponse create(CreateLocationRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı!"));

        Location location = locationMapper.toEntity(request);
        location.setUser(user);

        locationRepository.save(location);
        log.info("Location created: {} for user: {}", location.getName(), userId);

        return locationMapper.toResponse(location);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LocationResponse> getAll(Long userId, boolean onlyFavorites, Pageable pageable) {
        Page<Location> locations;

        if (onlyFavorites) {
            locations = locationRepository.findByUserIdAndFavoriteTrueAndDeletedFalseOrderByCreatedAtDesc(userId, pageable);
        } else {
            locations = locationRepository.findByUserIdAndDeletedFalseOrderByCreatedAtDesc(userId, pageable);
        }

        return locations.map(locationMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public LocationResponse getById(Long id, Long userId) {
        Location location = getLocationAndCheckOwnership(id, userId);
        return locationMapper.toResponse(location);
    }

    @Override
    @Transactional
    public LocationResponse update(Long id, UpdateLocationRequest request, Long userId) {
        Location location = getLocationAndCheckOwnership(id, userId);

        locationMapper.updateEntity(request, location);
        locationRepository.save(location);

        log.info("Location updated: {} for user: {}", id, userId);
        return locationMapper.toResponse(location);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        Location location = getLocationAndCheckOwnership(id, userId);
        location.softDelete();
        locationRepository.save(location);
        log.info("Location deleted: {} for user: {}", id, userId);
    }

    @Override
    @Transactional
    public LocationResponse toggleFavorite(Long id, Long userId) {
        Location location = getLocationAndCheckOwnership(id, userId);
        location.setFavorite(!location.isFavorite());
        locationRepository.save(location);
        log.info("Location favorite toggled: {} for user: {}", id, userId);
        return locationMapper.toResponse(location);
    }

    private Location getLocationAndCheckOwnership(Long id, Long userId) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Konum bulunamadı!"));

        if (location.isDeleted()) {
            throw new ResourceNotFoundException("Konum bulunamadı!");
        }

        if (!location.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Bu konuma erişim yetkiniz yok!");
        }

        return location;
    }

    @Override
    @Transactional
    public LocationResponse restore(Long id, Long userId) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Konum bulunamadı!"));

        if (!location.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Bu konuma erişim yetkiniz yok!");
        }

        if (!location.isDeleted()) {
            throw new BusinessException("Konum zaten aktif!");
        }

        location.restore();
        locationRepository.save(location);
        log.info("Location restored: {} for user: {}", id, userId);
        return locationMapper.toResponse(location);
    }
}