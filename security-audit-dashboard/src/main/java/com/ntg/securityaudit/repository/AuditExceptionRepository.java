package com.ntg.securityaudit.repository;

import com.ntg.securityaudit.entity.AuditException;
import com.ntg.securityaudit.enums.AuditExceptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface AuditExceptionRepository extends JpaRepository<AuditException, Long> {

    List<AuditException> findByRelatedAuditIdIn(Collection<Long> auditIds);

    List<AuditException> findByRelatedFindingId(Long findingId);

    List<AuditException> findByRelatedFindingIdIn(Collection<Long> findingIds);

    AuditException findTopByRelatedFindingIdOrderByIdDesc(Long findingId);

    List<AuditException> findAllByOrderByExpiryDateAsc();

    long countByStatus(AuditExceptionStatus status);

    List<AuditException> findByExpiryDateBetweenOrderByExpiryDateAsc(LocalDate start, LocalDate end);

}
