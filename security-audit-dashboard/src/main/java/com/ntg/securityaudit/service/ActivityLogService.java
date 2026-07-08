package com.ntg.securityaudit.service;

import com.ntg.securityaudit.entity.ActivityLog;
import com.ntg.securityaudit.repository.ActivityLogRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    public ActivityLogService(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    public List<ActivityLog> getAllLogs() {
        return activityLogRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public void log(String actionType,
                    String entityType,
                    Object entityId,
                    String entityName,
                    String oldValue,
                    String newValue,
                    String comment) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        ActivityLog log = new ActivityLog();
        log.setCreatedAt(LocalDateTime.now());
        log.setUsername(authentication != null ? authentication.getName() : "system");
        log.setRole(resolveRole(authentication));
        log.setActionType(actionType);
        log.setEntityType(entityType);
        log.setEntityId(entityId != null ? String.valueOf(entityId) : null);
        log.setEntityName(entityName);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setComment(comment);
        activityLogRepository.save(log);
    }

    private String resolveRole(Authentication authentication) {
        if (authentication == null) {
            return "SYSTEM";
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring("ROLE_".length()))
                .findFirst()
                .orElse("USER");
    }
}
