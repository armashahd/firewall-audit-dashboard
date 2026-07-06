package com.ntg.securityaudit.config;

import com.ntg.securityaudit.entity.Audit;
import com.ntg.securityaudit.entity.AuditException;
import com.ntg.securityaudit.entity.Finding;
import com.ntg.securityaudit.entity.Report;
import com.ntg.securityaudit.entity.Site;
import com.ntg.securityaudit.enums.AuditExceptionStatus;
import com.ntg.securityaudit.enums.FindingStatus;
import com.ntg.securityaudit.enums.Severity;
import com.ntg.securityaudit.enums.SiteStatus;
import com.ntg.securityaudit.repository.AuditExceptionRepository;
import com.ntg.securityaudit.repository.AuditRepository;
import com.ntg.securityaudit.repository.FindingRepository;
import com.ntg.securityaudit.repository.ReportRepository;
import com.ntg.securityaudit.repository.SiteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class DataInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final SiteRepository siteRepository;
    private final AuditRepository auditRepository;
    private final FindingRepository findingRepository;
    private final ReportRepository reportRepository;
    private final AuditExceptionRepository auditExceptionRepository;

    public DataInitializer(JdbcTemplate jdbcTemplate,
                           SiteRepository siteRepository,
                           AuditRepository auditRepository,
                           FindingRepository findingRepository,
                           ReportRepository reportRepository,
                           AuditExceptionRepository auditExceptionRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.siteRepository = siteRepository;
        this.auditRepository = auditRepository;
        this.findingRepository = findingRepository;
        this.reportRepository = reportRepository;
        this.auditExceptionRepository = auditExceptionRepository;
    }

    @Override
    public void run(String... args) {
        cleanupOrphanRecords();
        seedSites();
        seedAudits();
        seedFindings();
        normalizeAcceptedRiskFindings();
        seedReports();
        seedExceptions();
        normalizeAuditExceptions();
        refreshSiteSnapshots();
    }

    private void cleanupOrphanRecords() {
        jdbcTemplate.execute("DELETE FROM findings WHERE audit_id IS NULL OR audit_id NOT IN (SELECT id FROM audits)");
        jdbcTemplate.execute("DELETE FROM reports WHERE audit_id IS NULL OR audit_id NOT IN (SELECT id FROM audits)");
        jdbcTemplate.execute("DELETE FROM audit_exceptions WHERE audit_id IS NULL OR audit_id NOT IN (SELECT id FROM audits)");
        jdbcTemplate.execute("DELETE FROM audits WHERE site_id IS NULL OR site_id NOT IN (SELECT id FROM sites)");
        jdbcTemplate.execute("DELETE FROM findings WHERE audit_id IS NULL OR audit_id NOT IN (SELECT id FROM audits)");
        jdbcTemplate.execute("DELETE FROM reports WHERE audit_id IS NULL OR audit_id NOT IN (SELECT id FROM audits)");
        jdbcTemplate.execute("DELETE FROM audit_exceptions WHERE audit_id IS NULL OR audit_id NOT IN (SELECT id FROM audits)");
    }

    private void seedSites() {
        List<SiteSeed> siteSeeds = List.of(
                new SiteSeed("Colombo HQ", "Sri Lanka", "Colombo", "Fortinet", "FortiGate 100F", "7.4.8", 92, SiteStatus.ACTIVE),
                new SiteSeed("Kandy Branch", "Sri Lanka", "Kandy", "Palo Alto", "PA-3220", "10.2.4", 84, SiteStatus.ACTIVE),
                new SiteSeed("Galle Branch", "Sri Lanka", "Galle", "Cisco", "Firepower 2130", "7.2.5", 79, SiteStatus.UNDER_AUDIT),
                new SiteSeed("Jaffna Branch", "Sri Lanka", "Jaffna", "Fortinet", "FortiGate 60F", "7.2.8", 76, SiteStatus.ACTIVE),
                new SiteSeed("Kurunegala DC", "Sri Lanka", "Kurunegala", "Check Point", "Quantum 6200", "R81.20", 81, SiteStatus.ACTIVE),
                new SiteSeed("Negombo POP", "Sri Lanka", "Negombo", "Sophos", "XGS 3300", "19.5", 73, SiteStatus.ACTIVE),
                new SiteSeed("Matara Edge", "Sri Lanka", "Matara", "Fortinet", "FortiGate 80F", "7.0.14", 68, SiteStatus.UNDER_AUDIT),
                new SiteSeed("Batticaloa Office", "Sri Lanka", "Batticaloa", "Cisco", "ASA 5516-X", "9.18.4", 71, SiteStatus.ACTIVE)
        );

        Map<String, Site> existingSites = siteRepository.findAll().stream()
                .collect(Collectors.toMap(site -> site.getName().toLowerCase(Locale.ENGLISH), site -> site, (left, right) -> left, LinkedHashMap::new));

        for (SiteSeed seed : siteSeeds) {
            if (existingSites.containsKey(seed.name().toLowerCase(Locale.ENGLISH))) {
                continue;
            }

            Site site = new Site();
            site.setName(seed.name());
            site.setCountry(seed.country());
            site.setCity(seed.city());
            site.setFirewallVendor(seed.vendor());
            site.setFirewallModel(seed.model());
            site.setFirmwareVersion(seed.firmwareVersion());
            site.setSecurityScore(seed.securityScore());
            site.setStatus(seed.status());
            siteRepository.save(site);
        }
    }

    private void seedAudits() {
        Map<String, Site> sitesByName = siteRepository.findAll().stream()
                .collect(Collectors.toMap(site -> site.getName().toLowerCase(Locale.ENGLISH), site -> site, (left, right) -> left, LinkedHashMap::new));

        List<AuditSeed> auditSeeds = List.of(
                new AuditSeed("colombo hq", 1, LocalDate.parse("2025-01-14"), "North Technology Group", 84, 62, "Baseline perimeter review and hardening plan."),
                new AuditSeed("colombo hq", 2, LocalDate.parse("2025-05-20"), "North Technology Group", 88, 71, "Authentication and logging improvements applied."),
                new AuditSeed("colombo hq", 3, LocalDate.parse("2026-02-18"), "Internal Security Team", 91, 79, "Latest verification round with reduced exposure."),
                new AuditSeed("kandy branch", 1, LocalDate.parse("2025-02-11"), "North Technology Group", 78, 58, "Initial audit identified open management services."),
                new AuditSeed("kandy branch", 2, LocalDate.parse("2025-08-06"), "Internal Security Team", 82, 66, "MFA rollout improved access control."),
                new AuditSeed("kandy branch", 3, LocalDate.parse("2026-03-11"), "Internal Security Team", 86, 73, "Firmware and certificate review completed."),
                new AuditSeed("galle branch", 1, LocalDate.parse("2025-03-05"), "North Technology Group", 74, 52, "Firewall rules required cleanup."),
                new AuditSeed("galle branch", 2, LocalDate.parse("2025-09-18"), "Internal Security Team", 77, 61, "Logging retention and account review performed."),
                new AuditSeed("galle branch", 3, LocalDate.parse("2026-04-02"), "Internal Security Team", 80, 69, "Current round highlights residual medium risks."),
                new AuditSeed("jaffna branch", 1, LocalDate.parse("2025-03-28"), "North Technology Group", 72, 49, "Initial audit showed weak password controls."),
                new AuditSeed("jaffna branch", 2, LocalDate.parse("2025-10-22"), "Internal Security Team", 76, 56, "Certificate and backup issues were addressed."),
                new AuditSeed("jaffna branch", 3, LocalDate.parse("2026-05-10"), "Internal Security Team", 79, 64, "Current controls are stable with improvement backlog."),
                new AuditSeed("kurunegala dc", 1, LocalDate.parse("2025-04-16"), "North Technology Group", 87, 68, "Data center review confirmed strong segmentation."),
                new AuditSeed("kurunegala dc", 2, LocalDate.parse("2025-11-14"), "Internal Security Team", 89, 74, "Patch compliance and monitoring further improved."),
                new AuditSeed("negombo pop", 1, LocalDate.parse("2025-05-08"), "North Technology Group", 69, 45, "Access control and SNMP configuration required work."),
                new AuditSeed("negombo pop", 2, LocalDate.parse("2025-12-09"), "Internal Security Team", 74, 55, "High-risk findings were reduced after remediation."),
                new AuditSeed("matara edge", 1, LocalDate.parse("2025-06-03"), "North Technology Group", 67, 42, "Edge device review identified firmware drift."),
                new AuditSeed("matara edge", 2, LocalDate.parse("2026-01-20"), "Internal Security Team", 71, 53, "Operational controls improved but logging remains weak."),
                new AuditSeed("batticaloa office", 1, LocalDate.parse("2025-07-15"), "North Technology Group", 73, 50, "Office firewall review flagged permissive rules."),
                new AuditSeed("batticaloa office", 2, LocalDate.parse("2026-02-28"), "Internal Security Team", 78, 60, "Improvement plan is on track for closure.")
        );

        for (AuditSeed seed : auditSeeds) {
            Site site = sitesByName.get(seed.siteName());
            if (site == null) {
                continue;
            }

            boolean exists = auditRepository.findAll().stream().anyMatch(existing -> existing.getSite() != null
                    && existing.getSite().getName() != null
                    && existing.getSite().getName().equalsIgnoreCase(site.getName())
                    && Objects.equals(existing.getAuditRound(), seed.round())
                    && Objects.equals(existing.getAuditDate(), seed.auditDate()));
            if (exists) {
                continue;
            }

            Audit audit = new Audit();
            audit.setSite(site);
            audit.setAuditRound(seed.round());
            audit.setAuditDate(seed.auditDate());
            audit.setAuditor(seed.auditor());
            audit.setOverallScore(seed.overallScore());
            audit.setCompletionPercentage(seed.completionPercentage());
            audit.setRemarks(seed.remarks());
            auditRepository.save(audit);
        }
    }

    private void seedFindings() {
        List<FindingSeed> findingTemplates = List.of(
                new FindingSeed("Weak Password Policy", "Weak password policy on local and administrative accounts.", Severity.HIGH, FindingStatus.OPEN, "Authentication", "Enforce password complexity, rotation, and lockout policies.", "Security Team", 30),
                new FindingSeed("Missing MFA", "Privileged access lacks multi-factor authentication.", Severity.CRITICAL, FindingStatus.OPEN, "Authentication", "Require MFA for all privileged and remote access.", "IAM Team", 20),
                new FindingSeed("Outdated Firmware", "Firewall firmware is behind the supported release level.", Severity.HIGH, FindingStatus.IN_PROGRESS, "Patch Management", "Upgrade firewalls to the latest supported firmware release.", "Infrastructure Team", 21),
                new FindingSeed("Open Management Ports", "Administrative services are exposed to broad networks.", Severity.CRITICAL, FindingStatus.OPEN, "Firewall Configuration", "Restrict administrative access to trusted jump hosts only.", "Network Team", 14),
                new FindingSeed("Disabled Logging", "Security event logging is not enabled on key firewalls.", Severity.MEDIUM, FindingStatus.IN_PROGRESS, "Logging & Monitoring", "Enable centralized logging and alerting for all firewall events.", "SOC Team", 24),
                new FindingSeed("Unused Admin Accounts", "Inactive administrative accounts remain enabled.", Severity.HIGH, FindingStatus.OPEN, "Authentication", "Remove stale administrative accounts and review ownership.", "Security Team", 18),
                new FindingSeed("Permissive Firewall Rules", "Firewall policy allows wider access than required.", Severity.CRITICAL, FindingStatus.OPEN, "Firewall Configuration", "Tighten inbound and outbound ACLs to business-approved flows.", "Network Team", 16),
                new FindingSeed("Expired Certificates", "Expired or near-expiry certificates are still in use.", Severity.MEDIUM, FindingStatus.IN_PROGRESS, "Network Security", "Renew certificates and automate expiry monitoring.", "Platform Team", 26),
                new FindingSeed("Backup Failures", "Recent backup jobs failed validation checks.", Severity.HIGH, FindingStatus.OPEN, "Backup & Recovery", "Validate backup job success and test restoration procedures.", "Operations Team", 12),
                new FindingSeed("Weak SNMP Configuration", "SNMPv2 is still enabled with weak community strings.", Severity.MEDIUM, FindingStatus.ACCEPTED_RISK, "Network Security", "Upgrade SNMPv2 configurations to SNMPv3 with strong credentials.", "Network Team", 45)
        );

        List<Audit> orderedAudits = auditRepository.findAll().stream()
                .filter(audit -> audit.getSite() != null && audit.getAuditRound() != null && audit.getAuditDate() != null)
                .sorted(Comparator.comparing((Audit audit) -> audit.getAuditDate())
                        .thenComparing(audit -> audit.getAuditRound() != null ? audit.getAuditRound() : 0))
                .toList();

        int templateIndex = 0;
        for (Audit audit : orderedAudits) {
            FindingSeed primary = findingTemplates.get(templateIndex % findingTemplates.size());
            FindingSeed secondary = findingTemplates.get((templateIndex + 1) % findingTemplates.size());
            templateIndex += 2;

            createFindingIfMissing(audit, primary, 1);
            createFindingIfMissing(audit, secondary, 2);
        }
    }

    private void normalizeAcceptedRiskFindings() {
        for (Finding finding : findingRepository.findAll()) {
            if (finding.getStatus() != FindingStatus.ACCEPTED_RISK || finding.getId() == null) {
                continue;
            }

            AuditException linkedException = auditExceptionRepository.findTopByRelatedFindingIdOrderByIdDesc(finding.getId());
            if (linkedException != null) {
                if (linkedException.getStatus() != AuditExceptionStatus.CLOSED) {
                    linkedException.setStatus(AuditExceptionStatus.ACTIVE);
                    auditExceptionRepository.save(linkedException);
                }
                continue;
            }

            AuditException exceptionRecord = new AuditException();
            exceptionRecord.setRelatedSite(finding.getAudit() != null ? finding.getAudit().getSite() : null);
            exceptionRecord.setRelatedAudit(finding.getAudit());
            exceptionRecord.setRelatedFinding(finding);
            exceptionRecord.setExceptionName(finding.getTitle());
            exceptionRecord.setDescription(firstNonBlank(finding.getRecommendation(), finding.getDescription()));
            exceptionRecord.setJustification(firstNonBlank(finding.getRecommendation(), finding.getDescription()));
            exceptionRecord.setApprovedBy("Security Team");
            exceptionRecord.setApprovalDate(LocalDate.now());
            exceptionRecord.setExpiryDate(LocalDate.now().plusDays(90));
            exceptionRecord.setStatus(AuditExceptionStatus.ACTIVE);
            auditExceptionRepository.save(exceptionRecord);
        }
    }

    private void createFindingIfMissing(Audit audit, FindingSeed seed, int offset) {
        boolean exists = findingRepository.findAll().stream().anyMatch(existing -> existing.getAudit() != null
                && existing.getAudit().getId() != null
                && audit.getId() != null
                && existing.getAudit().getId().equals(audit.getId())
                && existing.getTitle() != null
                && existing.getTitle().equalsIgnoreCase(seed.title()));

        if (exists) {
            return;
        }

        Finding finding = new Finding();
        finding.setAudit(audit);
        finding.setTitle(seed.title());
        finding.setDescription(seed.description());
        finding.setSeverity(seed.severity());
        finding.setStatus(seed.status());
        finding.setCategory(seed.category());
        finding.setRecommendation(seed.recommendation());
        finding.setAssignedTo(seed.assignedTo());
        finding.setDueDate(audit.getAuditDate().plusDays(seed.dueDateOffset() + offset));
        if (seed.status() == FindingStatus.CLOSED) {
            finding.setClosedDate(audit.getAuditDate().plusDays(seed.dueDateOffset() - 2));
        }
        findingRepository.save(finding);
    }

    private void seedReports() {
        List<ReportSeed> reportSeeds = List.of(
                new ReportSeed("colombo hq", 1, "audit-report-colombo-q1.pdf", "v1.0", LocalDate.parse("2025-01-18"), "North Technology Group"),
                new ReportSeed("colombo hq", 2, "audit-report-colombo-q2.pdf", "v1.1", LocalDate.parse("2025-05-23"), "North Technology Group"),
                new ReportSeed("colombo hq", 3, "audit-report-colombo-q3.pdf", "v1.2", LocalDate.parse("2026-02-20"), "Internal Security Team"),
                new ReportSeed("kandy branch", 1, "audit-report-kandy-q1.pdf", "v1.0", LocalDate.parse("2025-02-13"), "North Technology Group"),
                new ReportSeed("galle branch", 1, "audit-report-galle-q1.pdf", "v1.0", LocalDate.parse("2025-03-07"), "North Technology Group"),
                new ReportSeed("jaffna branch", 2, "audit-report-jaffna-q2.pdf", "v1.1", LocalDate.parse("2025-10-25"), "Internal Security Team"),
                new ReportSeed("kurunegala dc", 2, "audit-report-kurunegala-q2.pdf", "v1.1", LocalDate.parse("2025-11-18"), "Internal Security Team"),
                new ReportSeed("negombo pop", 2, "audit-report-negombo-q2.pdf", "v1.1", LocalDate.parse("2025-12-11"), "Internal Security Team"),
                new ReportSeed("matara edge", 2, "audit-report-matara-q2.pdf", "v1.1", LocalDate.parse("2026-01-22"), "Internal Security Team"),
                new ReportSeed("batticaloa office", 2, "audit-report-batticaloa-q2.pdf", "v1.1", LocalDate.parse("2026-03-03"), "Internal Security Team")
        );

        Map<String, Audit> auditsByKey = auditRepository.findAll().stream()
                .filter(audit -> audit.getSite() != null && audit.getAuditRound() != null)
                .collect(Collectors.toMap(this::auditKey, audit -> audit, (left, right) -> left, LinkedHashMap::new));

        for (ReportSeed seed : reportSeeds) {
            Audit audit = auditsByKey.get(seed.siteName() + "|" + seed.round());
            if (audit == null) {
                continue;
            }

            boolean exists = reportRepository.findAll().stream()
                    .anyMatch(report -> report.getFileName() != null && report.getFileName().equalsIgnoreCase(seed.fileName()));
            if (exists) {
                continue;
            }

            Report report = new Report();
            report.setAudit(audit);
            report.setFileName(seed.fileName());
            report.setFilePath("reports/" + seed.fileName());
            report.setVersion(seed.version());
            report.setUploadDate(seed.uploadDate());
            report.setUploadedBy(seed.uploadedBy());
            reportRepository.save(report);
        }
    }

    private void seedExceptions() {
        Map<String, Audit> auditsByKey = auditRepository.findAll().stream()
                .filter(audit -> audit.getSite() != null && audit.getAuditRound() != null)
                .collect(Collectors.toMap(this::auditKey, audit -> audit, (left, right) -> left, LinkedHashMap::new));

        List<ExceptionSeed> exceptionSeeds = List.of(
                new ExceptionSeed("colombo hq", 1, "Legacy TLS Support Approved Until Migration", "Temporary approval for legacy remote access during migration.", "CISO Office", LocalDate.parse("2025-01-20"), LocalDate.parse("2025-04-20"), AuditExceptionStatus.CLOSED),
                new ExceptionSeed("colombo hq", 2, "Admin Service Exception", "Approval to retain a vendor maintenance account until cutover completes.", "Infrastructure Manager", LocalDate.parse("2025-05-25"), LocalDate.parse("2025-08-25"), AuditExceptionStatus.EXPIRED),
                new ExceptionSeed("kandy branch", 2, "Legacy VPN Encryption Exception", "Site requires a temporary VPN exception until network redesign is complete.", "Security Lead", LocalDate.parse("2025-08-08"), LocalDate.parse("2025-11-08"), AuditExceptionStatus.ACTIVE),
                new ExceptionSeed("galle branch", 3, "Delayed MFA Rollout Approval", "MFA rollout delayed due to operational dependency.", "SOC Manager", LocalDate.parse("2026-04-05"), LocalDate.parse("2026-06-30"), AuditExceptionStatus.ACTIVE),
                new ExceptionSeed("jaffna branch", 1, "Unsupported Firewall Firmware Temporarily Accepted", "Firmware patch deferred to avoid peak business hours disruption.", "Operations Head", LocalDate.parse("2025-03-31"), LocalDate.parse("2025-06-30"), AuditExceptionStatus.EXPIRED),
                new ExceptionSeed("kurunegala dc", 2, "Certificate Renewal Exception", "Certificate renewal depends on external CA timeline.", "Platform Owner", LocalDate.parse("2025-11-16"), LocalDate.parse("2026-02-16"), AuditExceptionStatus.ACTIVE),
                new ExceptionSeed("negombo pop", 1, "Temporary Admin Account Exception", "Temporary acceptance of privileged account access until tooling is upgraded.", "Network Manager", LocalDate.parse("2025-05-10"), LocalDate.parse("2025-08-10"), AuditExceptionStatus.CLOSED),
                new ExceptionSeed("matara edge", 2, "Logging Exception", "Edge firewall logging retained in local buffer pending SIEM integration.", "SOC Manager", LocalDate.parse("2026-01-22"), LocalDate.parse("2026-04-22"), AuditExceptionStatus.ACTIVE),
                new ExceptionSeed("batticaloa office", 1, "Firewall Rule Exception", "A business-critical inbound rule requires temporary approval.", "Regional IT Lead", LocalDate.parse("2025-07-18"), LocalDate.parse("2025-10-18"), AuditExceptionStatus.EXPIRED),
                new ExceptionSeed("batticaloa office", 2, "Backup Exception", "Backup retention temporarily reduced to fit current storage capacity.", "Operations Head", LocalDate.parse("2026-03-02"), LocalDate.parse("2026-06-02"), AuditExceptionStatus.ACTIVE)
        );

        for (ExceptionSeed seed : exceptionSeeds) {
            Audit audit = auditsByKey.get(seed.siteName() + "|" + seed.round());
            if (audit == null) {
                continue;
            }

            boolean exists = auditExceptionRepository.findAll().stream()
                    .anyMatch(exceptionRecord -> exceptionRecord.getTitle() != null
                            && exceptionRecord.getTitle().equalsIgnoreCase(seed.title()));
            if (exists) {
                continue;
            }

            AuditException exceptionRecord = new AuditException();
            exceptionRecord.setRelatedSite(audit.getSite());
            exceptionRecord.setRelatedAudit(audit);
            exceptionRecord.setExceptionName(seed.title());
            exceptionRecord.setDescription(seed.reason());
            exceptionRecord.setJustification(seed.reason());
            exceptionRecord.setApprovedBy(seed.approvedBy());
            exceptionRecord.setApprovalDate(seed.approvedDate());
            exceptionRecord.setExpiryDate(seed.expiryDate());
            exceptionRecord.setStatus(seed.status());
            auditExceptionRepository.save(exceptionRecord);
        }
    }

    private void normalizeAuditExceptions() {
        for (AuditException exceptionRecord : auditExceptionRepository.findAll()) {
            if (exceptionRecord.getRelatedAudit() != null && exceptionRecord.getRelatedSite() == null) {
                exceptionRecord.setRelatedSite(exceptionRecord.getRelatedAudit().getSite());
            }
            if (exceptionRecord.getStatus() == null) {
                if (exceptionRecord.getExpiryDate() != null && exceptionRecord.getExpiryDate().isBefore(LocalDate.now())) {
                    exceptionRecord.setStatus(AuditExceptionStatus.EXPIRED);
                } else {
                    exceptionRecord.setStatus(AuditExceptionStatus.ACTIVE);
                }
            }
            if (exceptionRecord.getDescription() == null) {
                exceptionRecord.setDescription(exceptionRecord.getJustification());
            }
            if (exceptionRecord.getJustification() == null) {
                exceptionRecord.setJustification(exceptionRecord.getReason());
            }
            if (exceptionRecord.getExceptionName() == null) {
                exceptionRecord.setExceptionName(exceptionRecord.getTitle());
            }
            if (exceptionRecord.getApprovalDate() == null) {
                exceptionRecord.setApprovalDate(exceptionRecord.getApprovedDate());
            }
            auditExceptionRepository.save(exceptionRecord);
        }
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        if (second != null && !second.isBlank()) {
            return second;
        }
        return null;
    }

    private void refreshSiteSnapshots() {
        List<Site> sites = siteRepository.findAll();
        List<Audit> audits = auditRepository.findAll();

        for (Site site : sites) {
            List<Audit> siteAudits = audits.stream()
                    .filter(audit -> audit.getSite() != null
                            && audit.getSite().getId() != null
                            && site.getId() != null
                            && audit.getSite().getId().equals(site.getId()))
                    .sorted(Comparator.comparing(Audit::getAuditDate, Comparator.nullsLast(Comparator.naturalOrder()))
                            .reversed()
                            .thenComparing(audit -> audit.getAuditRound() != null ? audit.getAuditRound() : 0, Comparator.reverseOrder()))
                    .toList();

            if (siteAudits.isEmpty()) {
                continue;
            }

            Audit latestAudit = siteAudits.getFirst();
            site.setLastAuditDate(latestAudit.getAuditDate());
            site.setSecurityScore(latestAudit.getOverallScore());
            if (latestAudit.getOverallScore() != null && latestAudit.getOverallScore() >= 85) {
                site.setStatus(SiteStatus.ACTIVE);
            } else {
                site.setStatus(SiteStatus.UNDER_AUDIT);
            }
            siteRepository.save(site);
        }
    }

    private String auditKey(Audit audit) {
        String siteName = audit.getSite() != null && audit.getSite().getName() != null ? audit.getSite().getName().toLowerCase(Locale.ENGLISH) : "-";
        return siteName + "|" + audit.getAuditRound();
    }

    private record SiteSeed(String name, String country, String city, String vendor, String model, String firmwareVersion, Integer securityScore, SiteStatus status) {
    }

    private record AuditSeed(String siteName, Integer round, LocalDate auditDate, String auditor, Integer overallScore, Integer completionPercentage, String remarks) {
    }

    private record FindingSeed(String title, String description, Severity severity, FindingStatus status, String category, String recommendation, String assignedTo, int dueDateOffset) {
    }

    private record ReportSeed(String siteName, Integer round, String fileName, String version, LocalDate uploadDate, String uploadedBy) {
    }

    private record ExceptionSeed(String siteName, Integer round, String title, String reason, String approvedBy, LocalDate approvedDate, LocalDate expiryDate, AuditExceptionStatus status) {
    }
}
