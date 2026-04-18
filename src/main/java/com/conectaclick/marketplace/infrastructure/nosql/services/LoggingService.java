package com.conectaclick.marketplace.infrastructure.nosql.services;

import com.conectaclick.marketplace.infrastructure.nosql.entities.ActivityLogEntity;
import com.conectaclick.marketplace.infrastructure.nosql.repositories.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoggingService {

    private final ActivityLogRepository activityLogRepository;
    
    @Value("${logging.mongodb.enabled:true}")
    private boolean mongodbEnabled;

    @Async("loggingExecutor")
    public void logActivity(Long userId, String action, String entityType,
                            Long entityId, String details, String level) {
        try {
            String logMessage = String.format("LOGGING - %s: User: %s - Entity: %s - Details: %s", 
                                           action, userId, entityType, details);
            
            if (mongodbEnabled) {
                // Intentar guardar en MongoDB
                log.info("Attempting to log activity to MongoDB: {}", logMessage);
                
                // Obtener información de la petición HTTP actual (si existe)
                String ipAddress = getClientIp();
                String userAgent = getUserAgent();

                ActivityLogEntity logEntry = ActivityLogEntity.builder()
                        .userId(userId)
                        .action(action)
                        .entityType(entityType)
                        .entityId(entityId)
                        .details(details)
                        .level(level)
                        .createdAt(LocalDateTime.now())
                        .ipAddress(ipAddress)
                        .userAgent(userAgent)
                        .build();

                ActivityLogEntity saved = activityLogRepository.save(logEntry);
                log.info("Successfully saved log entry to MongoDB with ID: {}", saved.getId());
                
            } else {
                // Fallback a consola si MongoDB está deshabilitado
                log.info("LOGGING (MongoDB disabled): {}", logMessage);
            }
            
        } catch (Exception e) {
            // Siempre loguear el error, sin importar si MongoDB está habilitado
            log.error("Failed to log activity - User: {} - Action: {} - Error: {}", 
                      userId, action, e.getMessage(), e);
            
            // No lanzamos excepción para no afectar el flujo principal
        }
    }

    private String getClientIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isEmpty()) {
                    ip = request.getRemoteAddr();
                }
                return ip;
            }
        } catch (Exception e) {
            log.debug("Could not get client IP", e);
        }
        return "unknown";
    }

    private String getUserAgent() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return request.getHeader("User-Agent");
            }
        } catch (Exception e) {
            log.debug("Could not get User-Agent", e);
        }
        return "unknown";
    }
}
