package com.altankoc.rotame.location.repository;

import com.altankoc.rotame.location.entity.Location;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    Page<Location> findByUserIdAndDeletedFalseOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Page<Location> findByUserIdAndFavoriteTrueAndDeletedFalseOrderByCreatedAtDesc(Long userId, Pageable pageable);
    int deleteByDeletedTrueAndUpdatedAtBefore(LocalDateTime date);
}
