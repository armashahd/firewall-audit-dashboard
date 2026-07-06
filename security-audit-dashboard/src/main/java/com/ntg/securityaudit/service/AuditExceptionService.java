package com.ntg.securityaudit.service;

import com.ntg.securityaudit.entity.AuditException;
import com.ntg.securityaudit.enums.AuditExceptionStatus;
import com.ntg.securityaudit.repository.AuditExceptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class AuditExceptionService {

    private final AuditExceptionRepository auditExceptionRepository;
    private final DatabaseRepairService databaseRepairService;

    public AuditExceptionService(AuditExceptionRepository auditExceptionRepository,
                                 DatabaseRepairService databaseRepairService) {
        this.auditExceptionRepository = auditExceptionRepository;
        this.databaseRepairService = databaseRepairService;
    }

    @Transactional
    public List<AuditException> getAllAuditExceptions() {
        databaseRepairService.repairIfNeeded();
        normalizeStatuses();
        return auditExceptionRepository.findAllByOrderByExpiryDateAsc();
    }

    @Transactional
    public AuditException getAuditExceptionById(Long id) {
        databaseRepairService.repairIfNeeded();
        normalizeStatuses();
        return auditExceptionRepository.findById(id).orElse(null);
    }

    public AuditException saveAuditException(AuditException auditException) {
        if (auditException.getStatus() != AuditExceptionStatus.CLOSED) {
            auditException.setStatus(auditException.getExpiryDate() != null && auditException.getExpiryDate().isBefore(LocalDate.now())
                    ? AuditExceptionStatus.EXPIRED
                    : AuditExceptionStatus.ACTIVE);
        }
        return auditExceptionRepository.save(auditException);
    }

    public void deleteAuditException(Long id) {
        if (id != null && auditExceptionRepository.existsById(id)) {
            auditExceptionRepository.deleteById(id);
        }
    }

    public long countByStatus(AuditExceptionStatus status) {
        databaseRepairService.repairIfNeeded();
        return auditExceptionRepository.countByStatus(status);
    }

    public List<AuditException> getUpcomingExpiryExceptions() {
        databaseRepairService.repairIfNeeded();
        normalizeStatuses();
        return auditExceptionRepository.findByExpiryDateBetweenOrderByExpiryDateAsc(LocalDate.now(), LocalDate.now().plusDays(60));
    }

    private void normalizeStatuses() {
        List<AuditException> changedExceptions = auditExceptionRepository.findAll().stream()
                .filter(exception -> exception.getStatus() != AuditExceptionStatus.CLOSED)
                .filter(exception -> exception.getExpiryDate() != null)
                .filter(exception -> {
                    AuditExceptionStatus expectedStatus = exception.getExpiryDate().isBefore(LocalDate.now())
                            ? AuditExceptionStatus.EXPIRED
                            : AuditExceptionStatus.ACTIVE;
                    return exception.getStatus() != expectedStatus;
                })
                .peek(exception -> exception.setStatus(exception.getExpiryDate().isBefore(LocalDate.now())
                        ? AuditExceptionStatus.EXPIRED
                        : AuditExceptionStatus.ACTIVE))
                .toList();
        if (!changedExceptions.isEmpty()) {
            auditExceptionRepository.saveAll(changedExceptions);
        }
    }
}
