package com.ntg.securityaudit.service;

import com.ntg.securityaudit.dto.DashboardDTO;
import com.ntg.securityaudit.enums.FindingStatus;
import com.ntg.securityaudit.enums.Severity;
import com.ntg.securityaudit.repository.AuditRepository;
import com.ntg.securityaudit.repository.FindingRepository;
import com.ntg.securityaudit.repository.SiteRepository;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private final SiteRepository siteRepository;
    private final AuditRepository auditRepository;
    private final FindingRepository findingRepository;

    public DashboardService(SiteRepository siteRepository,
                            AuditRepository auditRepository,
                            FindingRepository findingRepository) {
        this.siteRepository = siteRepository;
        this.auditRepository = auditRepository;
        this.findingRepository = findingRepository;
    }

    public DashboardDTO getDashboardData() {

        DashboardDTO dto = new DashboardDTO();

        long totalSites = siteRepository.count();
        long totalAudits = auditRepository.count();

        long openFindings = findingRepository.countByStatusIn(
                java.util.List.of(FindingStatus.OPEN, FindingStatus.IN_PROGRESS));

        long closedFindings = findingRepository.countByStatus(FindingStatus.CLOSED);

        long criticalFindings = findingRepository.countBySeverity(Severity.CRITICAL);
        long highFindings = findingRepository.countBySeverity(Severity.HIGH);
        long mediumFindings = findingRepository.countBySeverity(Severity.MEDIUM);
        long lowFindings = findingRepository.countBySeverity(Severity.LOW);

        Double averageScore = siteRepository.averageSecurityScore();
        Double averageCompletion = auditRepository.averageCompletionPercentage();

        dto.setOverallSecurityScore(
                averageScore != null ? (int) Math.round(averageScore) : 0);

        dto.setCompletionPercentage(
                averageCompletion != null ? (int) Math.round(averageCompletion) : 0);

        dto.setTotalSites(totalSites);
        dto.setTotalAudits(totalAudits);
        dto.setOpenFindings(openFindings);
        dto.setClosedFindings(closedFindings);
        dto.setCriticalFindings(criticalFindings);
        dto.setHighFindings(highFindings);
        dto.setMediumFindings(mediumFindings);
        dto.setLowFindings(lowFindings);

        return dto;
    }
}
