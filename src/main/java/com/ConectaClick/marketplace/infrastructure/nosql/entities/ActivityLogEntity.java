package com.ConectaClick.marketplace.infrastructure.nosql.entities;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@Document(collection = "activity_logs")
public class ActivityLogEntity {

    @Id
    private String id;

    @Indexed
    private Long userId;

    private String action;

    private String entityType;

    private Long entityId;

    private String details;

    private String level; // INFO, WARN, ERROR

    @Indexed
    private LocalDateTime createdAt;

    private String ipAddress;

    private String userAgent;
}