package com.altankoc.rotame.core.scheduler;

import com.altankoc.rotame.location.repository.LocationRepository;
import com.altankoc.rotame.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class CleanupScheduler {

    private final UserRepository userRepository;
    private final LocationRepository locationRepository;

    // Her gün gece 02:00
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupDeletedLocations() {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        int count = locationRepository.deleteByDeletedTrueAndUpdatedAtBefore(oneWeekAgo);
        log.info("Cleanup: {} deleted locations permanently removed", count);
    }

    // Her gün gece 03:00
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupDeletedUsers() {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        int count = userRepository.deleteByDeletedTrueAndUpdatedAtBefore(oneMonthAgo);
        log.info("Cleanup: {} deleted users permanently removed", count);
    }
}