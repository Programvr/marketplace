package com.ConectaClick.marketplace.infrastructure.nosql.services;

import com.ConectaClick.marketplace.infrastructure.nosql.entities.ActivityLogEntity;
import com.ConectaClick.marketplace.infrastructure.nosql.repositories.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public void logActivity(Long userId, String action, String entityType,
                            Long entityId, String details, String level) {
        try {
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

            activityLogRepository.save(logEntry);

            log.debug("Activity logged: {} for user {} - Details: {}", action, userId, details);

        } catch (Exception e) {
            log.error("Failed to save activity log to MongoDB", e);
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