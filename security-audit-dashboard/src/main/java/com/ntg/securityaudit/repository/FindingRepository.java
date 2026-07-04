package com.ntg.securityaudit.repository;

import com.ntg.securityaudit.entity.Finding;
import com.ntg.securityaudit.enums.FindingStatus;
import com.ntg.securityaudit.enums.Severity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface FindingRepository extends JpaRepository<Finding, Long> {

    long countByStatus(FindingStatus status);

    long countByStatusIn(Collection<FindingStatus> statuses);

    long countBySeverity(Severity severity);

}
