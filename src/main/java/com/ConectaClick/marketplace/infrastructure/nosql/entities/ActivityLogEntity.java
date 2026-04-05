package com.ConectaClick.marketplace.infrastructure.nosql.entities;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@Builder
@Document(collection = "activity_logs")
public class ActivityLogEntity {
    
    @Id
    private String id;
    
    @Indexed
    private Long userId;
    
    @Indexed
    private String action;
    
    @Indexed
    private String entityType;
    
    @Indexed
    private Long entityId;
    
    private String details;
    
    private String level;
    
    @Field("createdAt")
    @Indexed
    private LocalDateTime createdAt;
    
    private String ipAddress;
    
    private String userAgent;
}
