package com.ntg.securityaudit.service;

import com.ntg.securityaudit.entity.Audit;
import com.ntg.securityaudit.repository.AuditExceptionRepository;
import com.ntg.securityaudit.repository.AuditRepository;
import com.ntg.securityaudit.repository.FindingRepository;
import com.ntg.securityaudit.repository.ReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditService {

    private final AuditRepository auditRepository;
    private final DatabaseRepairService databaseRepairService;
    private final FindingRepository findingRepository;
    private final ReportRepository reportRepository;
    private final AuditExceptionRepository auditExceptionRepository;

    public AuditService(AuditRepository auditRepository,
                        DatabaseRepairService databaseRepairService,
                        FindingRepository findingRepository,
                        ReportRepository reportRepository,
                        AuditExceptionRepository auditExceptionRepository) {
        this.auditRepository = auditRepository;
        this.databaseRepairService = databaseRepairService;
        this.findingRepository = findingRepository;
        this.reportRepository = reportRepository;
        this.auditExceptionRepository = auditExceptionRepository;
    }

    public List<Audit> getAllAudits() {
        databaseRepairService.repairIfNeeded();
        return auditRepository.findAll();
    }

    public Audit getAuditById(Long id) {
        databaseRepairService.repairIfNeeded();
        return auditRepository.findById(id).orElse(null);
    }

    public Audit saveAudit(Audit audit) {
        return auditRepository.save(audit);
    }

    @Transactional
    public void deleteAudit(Long id) {
        if (id != null && auditRepository.existsById(id)) {
            List<Long> auditIds = List.of(id);
            List<Long> findingIds = findingRepository.findByAuditIdIn(auditIds).stream()
                    .map(finding -> finding.getId())
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList());

            if (!findingIds.isEmpty()) {
                auditExceptionRepository.deleteAll(auditExceptionRepository.findByRelatedFindingIdIn(findingIds));
            }
            auditExceptionRepository.deleteAll(auditExceptionRepository.findByRelatedAuditIdIn(auditIds));
            reportRepository.deleteAll(reportRepository.findByAuditIdIn(auditIds));
            findingRepository.deleteAll(findingRepository.findByAuditIdIn(auditIds));
            auditRepository.deleteById(id);
        }
    }
}
