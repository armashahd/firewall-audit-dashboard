package com.ntg.securityaudit.repository;

import com.ntg.securityaudit.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    List<ActivityLog> findAllByOrderByCreatedAtDesc();

    void deleteByEntityTypeAndEntityIdIn(String entityType, Collection<String> entityIds);
}
