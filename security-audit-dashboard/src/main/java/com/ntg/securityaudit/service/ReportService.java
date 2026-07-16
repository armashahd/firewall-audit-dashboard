package com.ntg.securityaudit.service;

import com.ntg.securityaudit.entity.Audit;
import com.ntg.securityaudit.entity.Finding;
import com.ntg.securityaudit.entity.Report;
import com.ntg.securityaudit.repository.ActivityLogRepository;
import com.ntg.securityaudit.repository.AuditExceptionRepository;
import com.ntg.securityaudit.repository.AuditRepository;
import com.ntg.securityaudit.repository.FindingActivityLogRepository;
import com.ntg.securityaudit.repository.FindingRepository;
import com.ntg.securityaudit.repository.ReportRepository;
import com.ntg.securityaudit.repository.SiteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final DatabaseRepairService databaseRepairService;
    private final AuditRepository auditRepository;
    private final FindingRepository findingRepository;
    private final AuditExceptionRepository auditExceptionRepository;
    private final FindingActivityLogRepository findingActivityLogRepository;
    private final SiteRepository siteRepository;
    private final ActivityLogRepository activityLogRepository;

    public ReportService(ReportRepository reportRepository,
                         DatabaseRepairService databaseRepairService,
                         AuditRepository auditRepository,
                         FindingRepository findingRepository,
                         AuditExceptionRepository auditExceptionRepository,
                         FindingActivityLogRepository findingActivityLogRepository,
                         SiteRepository siteRepository,
                         ActivityLogRepository activityLogRepository) {
        this.reportRepository = reportRepository;
        this.databaseRepairService = databaseRepairService;
        this.auditRepository = auditRepository;
        this.findingRepository = findingRepository;
        this.auditExceptionRepository = auditExceptionRepository;
        this.findingActivityLogRepository = findingActivityLogRepository;
        this.siteRepository = siteRepository;
        this.activityLogRepository = activityLogRepository;
    }

    public List<Report> getAllReports() {
        databaseRepairService.repairIfNeeded();
        return reportRepository.findAll();
    }

    public Report getReportById(Long id) {
        databaseRepairService.repairIfNeeded();
        return reportRepository.findById(id).orElse(null);
    }

    public Report saveReport(Report report) {
        return reportRepository.save(report);
    }

    @Transactional
    public void deleteReport(Long id) {
        if (id == null) {
            return;
        }

        Report report = reportRepository.findById(id).orElse(null);
        if (report == null) {
            return;
        }

        Audit audit = report.getAudit();
        if (audit == null || audit.getId() == null) {
            deleteUploadedFiles(List.of(report));
            deleteActivityLogs("Report", List.of(report.getId()));
            reportRepository.delete(report);
            return;
        }

        deleteAuditData(audit);
    }

    private void deleteAuditData(Audit audit) {
        Long auditId = audit.getId();
        if (!auditRepository.existsById(auditId)) {
            return;
        }

        List<Long> auditIds = List.of(auditId);
        Long siteId = audit.getSite() != null ? audit.getSite().getId() : null;
        List<Report> reports = reportRepository.findByAuditIdIn(auditIds);
        List<Finding> findings = findingRepository.findByAuditIdIn(auditIds);
        List<Long> findingIds = findings.stream()
                .map(Finding::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<Long> reportIds = reports.stream()
                .map(Report::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<Long> exceptionIds = new ArrayList<>();
        exceptionIds.addAll(auditExceptionRepository.findByRelatedAuditIdIn(auditIds).stream()
                .map(exception -> exception.getId())
                .filter(Objects::nonNull)
                .toList());
        if (!findingIds.isEmpty()) {
            exceptionIds.addAll(auditExceptionRepository.findByRelatedFindingIdIn(findingIds).stream()
                    .map(exception -> exception.getId())
                    .filter(Objects::nonNull)
                    .toList());
        }

        if (!findingIds.isEmpty()) {
            auditExceptionRepository.deleteAll(auditExceptionRepository.findByRelatedFindingIdIn(findingIds));
            findingIds.forEach(findingActivityLogRepository::deleteByFindingId);
            deleteActivityLogs("Finding", findingIds);
        }
        auditExceptionRepository.deleteAll(auditExceptionRepository.findByRelatedAuditIdIn(auditIds));
        deleteActivityLogs("Audit Exception", exceptionIds);
        deleteActivityLogs("Report", reportIds);
        deleteActivityLogs("Audit", auditIds);
        findingRepository.deleteAll(findings);
        reportRepository.deleteAll(reports);
        deleteUploadedFiles(reports);
        auditRepository.deleteById(auditId);

        deleteSiteIfUnused(siteId);
    }

    private void deleteSiteIfUnused(Long siteId) {
        if (siteId == null || !siteRepository.existsById(siteId) || auditRepository.countBySiteId(siteId) > 0) {
            return;
        }

        deleteActivityLogs("Site", List.of(siteId));
        siteRepository.deleteById(siteId);
    }

    private void deleteUploadedFiles(List<Report> reports) {
        reports.stream()
                .map(Report::getFilePath)
                .filter(StringUtils::hasText)
                .map(Path::of)
                .forEach(this::deleteUploadedFile);
    }

    private void deleteUploadedFile(Path filePath) {
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {
            // The database delete should still complete if the uploaded file is already missing or locked.
        }
    }

    private void deleteActivityLogs(String entityType, List<Long> entityIds) {
        List<String> ids = entityIds.stream()
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .toList();
        if (!ids.isEmpty()) {
            activityLogRepository.deleteByEntityTypeAndEntityIdIn(entityType, ids);
        }
    }
}
