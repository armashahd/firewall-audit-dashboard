package com.ntg.securityaudit.repository;

import com.ntg.securityaudit.entity.Audit;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditRepository extends JpaRepository<Audit, Long> {

    @Query("select coalesce(avg(a.completionPercentage), 0) from Audit a")
    Double averageCompletionPercentage();

    List<Audit> findBySiteId(Long siteId);

}
