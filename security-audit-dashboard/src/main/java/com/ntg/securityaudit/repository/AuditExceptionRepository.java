package com.ntg.securityaudit.repository;

import com.ntg.securityaudit.entity.AuditException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditExceptionRepository extends JpaRepository<AuditException, Long> {

}