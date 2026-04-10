package com.altankoc.rotame.location.repository;

import com.altankoc.rotame.location.entity.LocationImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationImageRepository extends JpaRepository<LocationImage, Long> {
    List<LocationImage> findByLocationIdAndDeletedFalse(Long locationId);
    int countByLocationIdAndDeletedFalse(Long locationId);
}
