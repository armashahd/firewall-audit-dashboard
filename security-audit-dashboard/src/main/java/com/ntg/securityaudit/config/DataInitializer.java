package com.ntg.securityaudit.config;

import com.ntg.securityaudit.repository.SiteRepository;
import com.ntg.securityaudit.repository.AuditRepository;
import com.ntg.securityaudit.repository.FindingRepository;
import com.ntg.securityaudit.repository.ReportRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.ntg.securityaudit.entity.Audit;
import com.ntg.securityaudit.entity.Finding;
import com.ntg.securityaudit.entity.Report;
import com.ntg.securityaudit.entity.Site;
import com.ntg.securityaudit.enums.FindingStatus;
import com.ntg.securityaudit.enums.Severity;
import com.ntg.securityaudit.enums.SiteStatus;
import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private final SiteRepository siteRepository;
    private final AuditRepository auditRepository;
    private final FindingRepository findingRepository;
    private final ReportRepository reportRepository;

    public DataInitializer(SiteRepository siteRepository,
                           AuditRepository auditRepository,
                           FindingRepository findingRepository,
                           ReportRepository reportRepository) {
        this.siteRepository = siteRepository;
        this.auditRepository = auditRepository;
        this.findingRepository = findingRepository;
        this.reportRepository = reportRepository;
    }

    @Override
    public void run(String... args) {

        if (siteRepository.count() == 0) {
            System.out.println("Database is empty. Sample site will be inserted...");

            Site site = new Site();

            site.setName("Colombo HQ");
            site.setCountry("Sri Lanka");
            site.setCity("Colombo");
            site.setFirewallVendor("Fortinet");
            site.setFirewallModel("FortiGate 100F");
            site.setFirmwareVersion("7.4.8");
            site.setLastAuditDate(LocalDate.now());
            site.setSecurityScore(92);
            site.setStatus(SiteStatus.ACTIVE);

            siteRepository.save(site);

            System.out.println("Sample site inserted successfully.");
        }

        Site site = siteRepository.findAll().stream().findFirst().orElse(null);
        if (site == null) {
            return;
        }

        Audit audit = auditRepository.findAll().stream().findFirst().orElse(null);

        if (audit == null) {
            audit = new Audit();
            audit.setSite(site);
            audit.setAuditRound(1);
            audit.setAuditDate(LocalDate.now());
            audit.setAuditor("North Technology Group");
            audit.setOverallScore(92);
            audit.setCompletionPercentage(76);
            audit.setRemarks("Initial baseline audit.");
            audit = auditRepository.save(audit);
        }

        if (findingRepository.count() == 0 && audit != null) {
            Finding finding = new Finding();
            finding.setAudit(audit);
            finding.setTitle("Password policy requires strengthening");
            finding.setDescription("Several local accounts still use weak password rules.");
            finding.setSeverity(Severity.HIGH);
            finding.setStatus(FindingStatus.OPEN);
            finding.setCategory("Authentication");
            finding.setRecommendation("Enforce stronger password complexity and rotation controls.");
            finding.setAssignedTo("Security Team");
            finding.setDueDate(LocalDate.now().plusDays(30));
            findingRepository.save(finding);
        }

        if (reportRepository.count() == 0 && audit != null) {
            Report report = new Report();
            report.setAudit(audit);
            report.setFileName("audit-report-q1.pdf");
            report.setFilePath("reports/audit-report-q1.pdf");
            report.setVersion("v1.0");
            report.setUploadDate(LocalDate.now());
            report.setUploadedBy("North Technology Group");
            reportRepository.save(report);
        }
    }
}
