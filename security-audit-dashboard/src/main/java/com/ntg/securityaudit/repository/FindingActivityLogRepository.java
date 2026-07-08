package com.ntg.securityaudit.repository;

import com.ntg.securityaudit.entity.FindingActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FindingActivityLogRepository extends JpaRepository<FindingActivityLog, Long> {

    List<FindingActivityLog> findByFindingIdOrderByCreatedAtDesc(Long findingId);

    void deleteByFindingId(Long findingId);
}
