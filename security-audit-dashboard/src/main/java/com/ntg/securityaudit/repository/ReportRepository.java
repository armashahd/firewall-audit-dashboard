package com.ntg.securityaudit.repository;

import com.ntg.securityaudit.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findByAuditIdIn(Collection<Long> auditIds);

}
