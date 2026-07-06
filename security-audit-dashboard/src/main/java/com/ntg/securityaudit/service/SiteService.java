package com.ntg.securityaudit.service;

import com.ntg.securityaudit.entity.Audit;
import com.ntg.securityaudit.entity.Site;
import com.ntg.securityaudit.repository.AuditExceptionRepository;
import com.ntg.securityaudit.repository.AuditRepository;
import com.ntg.securityaudit.repository.FindingRepository;
import com.ntg.securityaudit.repository.ReportRepository;
import com.ntg.securityaudit.repository.SiteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SiteService {

    private final SiteRepository siteRepository;
    private final DatabaseRepairService databaseRepairService;
    private final AuditRepository auditRepository;
    private final FindingRepository findingRepository;
    private final ReportRepository reportRepository;
    private final AuditExceptionRepository auditExceptionRepository;

    public SiteService(SiteRepository siteRepository,
                       DatabaseRepairService databaseRepairService,
                       AuditRepository auditRepository,
                       FindingRepository findingRepository,
                       ReportRepository reportRepository,
                       AuditExceptionRepository auditExceptionRepository) {
        this.siteRepository = siteRepository;
        this.databaseRepairService = databaseRepairService;
        this.auditRepository = auditRepository;
        this.findingRepository = findingRepository;
        this.reportRepository = reportRepository;
        this.auditExceptionRepository = auditExceptionRepository;
    }

    public List<Site> getAllSites() {
        databaseRepairService.repairIfNeeded();
        return siteRepository.findAll();
    }

    public Site saveSite(Site site) {
        return siteRepository.save(site);
    }

    public Site getSiteById(Long id) {
        databaseRepairService.repairIfNeeded();
        return siteRepository.findById(id).orElse(null);
    }

    @Transactional
    public void deleteSite(Long id) {
        if (id != null && siteRepository.existsById(id)) {
            List<Audit> audits = auditRepository.findBySiteId(id);
            List<Long> auditIds = audits.stream()
                    .map(Audit::getId)
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList());

            if (!auditIds.isEmpty()) {
                findingRepository.deleteAll(findingRepository.findByAuditIdIn(auditIds));
                reportRepository.deleteAll(reportRepository.findByAuditIdIn(auditIds));
                auditExceptionRepository.deleteAll(auditExceptionRepository.findByAuditIdIn(auditIds));
                auditRepository.deleteAll(audits);
            }

            siteRepository.deleteById(id);
        }
    }

}
