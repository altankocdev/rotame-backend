package com.altankoc.rotame.location.service;

import com.altankoc.rotame.location.dto.CreateLocationRequest;
import com.altankoc.rotame.location.dto.LocationResponse;
import com.altankoc.rotame.location.dto.UpdateLocationRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LocationService {
    LocationResponse create(CreateLocationRequest request, Long userId);
    Page<LocationResponse> getAll(Long userId, boolean onlyFavorites, Pageable pageable);
    LocationResponse getById(Long id, Long userId);
    LocationResponse update(Long id, UpdateLocationRequest request, Long userId);
    void delete(Long id, Long userId);
    LocationResponse toggleFavorite(Long id, Long userId);
    LocationResponse restore(Long id, Long userId);
}