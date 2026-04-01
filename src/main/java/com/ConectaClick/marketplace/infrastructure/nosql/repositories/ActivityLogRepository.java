package com.ConectaClick.marketplace.infrastructure.nosql.repositories;

import com.ConectaClick.marketplace.infrastructure.nosql.entities.ActivityLogEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityLogRepository extends MongoRepository<ActivityLogEntity, String> {
    List<ActivityLogEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<ActivityLogEntity> findByActionAndCreatedAtBetween(String action, LocalDateTime start, LocalDateTime end);
    void deleteByCreatedAtBefore(LocalDateTime date);
}