package com.altankoc.rotame.location.service;

import com.altankoc.rotame.location.dto.LocationImageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface LocationImageService {
    LocationImageResponse upload(Long locationId, Long userId, MultipartFile file);
    List<LocationImageResponse> getAll(Long locationId, Long userId);
    void delete(Long locationId, Long imageId, Long userId);
    LocationImageResponse setCover(Long locationId, Long imageId, Long userId);
}