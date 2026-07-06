package com.ntg.securityaudit.service;

import com.ntg.securityaudit.dto.ParsedAuditReport;
import com.ntg.securityaudit.dto.ReportUploadResult;
import com.ntg.securityaudit.entity.Audit;
import com.ntg.securityaudit.entity.Report;
import com.ntg.securityaudit.entity.Site;
import com.ntg.securityaudit.enums.SiteStatus;
import com.ntg.securityaudit.repository.AuditRepository;
import com.ntg.securityaudit.repository.ReportRepository;
import com.ntg.securityaudit.repository.SiteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Service
public class ReportUploadService {

    private static final Path UPLOAD_DIR = Path.of("uploads");

    private final PdfExtractionService pdfExtractionService;
    private final FindingImportService findingImportService;
    private final SiteRepository siteRepository;
    private final AuditRepository auditRepository;
    private final ReportRepository reportRepository;

    public ReportUploadService(PdfExtractionService pdfExtractionService,
                               FindingImportService findingImportService,
                               SiteRepository siteRepository,
                               AuditRepository auditRepository,
                               ReportRepository reportRepository) {
        this.pdfExtractionService = pdfExtractionService;
        this.findingImportService = findingImportService;
        this.siteRepository = siteRepository;
        this.auditRepository = auditRepository;
        this.reportRepository = reportRepository;
    }

    @Transactional
    public ReportUploadResult uploadAuditReport(MultipartFile file) throws IOException {
        validatePdf(file);
        Files.createDirectories(UPLOAD_DIR);

        Path tempFile = Files.createTempFile(UPLOAD_DIR, "audit-import-", ".pdf");
        Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);

        ParsedAuditReport parsedReport = pdfExtractionService.extract(tempFile.toFile());
        normalizeRequiredMetadata(parsedReport, file.getOriginalFilename());

        if (isDuplicate(parsedReport)) {
            Files.deleteIfExists(tempFile);
            ReportUploadResult result = new ReportUploadResult();
            result.setDuplicate(true);
            result.setSiteName(parsedReport.getSiteName());
            result.setMessage("This report appears to be already imported.");
            return result;
        }

        Path finalPath = UPLOAD_DIR.resolve(uniqueFileName(file.getOriginalFilename()));
        Files.move(tempFile, finalPath, StandardCopyOption.REPLACE_EXISTING);

        Site site = createOrUpdateSite(parsedReport);
        Audit audit = createAudit(site, parsedReport);
        Report report = createReport(audit, parsedReport, finalPath, file.getOriginalFilename());
        int findingCount = findingImportService.importFindings(audit, parsedReport.getFindings());

        ReportUploadResult result = new ReportUploadResult();
        result.setReportId(report.getId());
        result.setSiteName(site.getName());
        result.setAuditRound(audit.getAuditRound());
        result.setFindingCount(findingCount);
        result.setMessage("Audit report imported successfully.");
        return result;
    }

    private void validatePdf(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please select a PDF audit report to upload.");
        }
        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase(Locale.ENGLISH) : "";
        if (!fileName.endsWith(".pdf")) {
            throw new IllegalArgumentException("Only PDF audit reports are supported.");
        }
    }

    private void normalizeRequiredMetadata(ParsedAuditReport parsedReport, String originalFileName) {
        if (!StringUtils.hasText(parsedReport.getSiteName())) {
            parsedReport.setSiteName(fileBaseName(originalFileName));
        }
        if (parsedReport.getAuditDate() == null) {
            parsedReport.setAuditDate(LocalDate.now());
        }
        if (!StringUtils.hasText(parsedReport.getReportVersion())) {
            parsedReport.setReportVersion("v1.0");
        }
        if (!StringUtils.hasText(parsedReport.getAuditor())) {
            parsedReport.setAuditor("Imported Report");
        }
    }

    private boolean isDuplicate(ParsedAuditReport parsedReport) {
        return reportRepository.findAll().stream()
                .anyMatch(report -> sameReportIdentity(report, parsedReport));
    }

    private boolean sameReportIdentity(Report report, ParsedAuditReport parsedReport) {
        LocalDate existingAuditDate = report.getAuditDate() != null
                ? report.getAuditDate()
                : report.getAudit() != null ? report.getAudit().getAuditDate() : null;
        String existingSiteName = firstNonBlank(report.getSiteName(),
                report.getAudit() != null && report.getAudit().getSite() != null ? report.getAudit().getSite().getName() : null);

        boolean dateMatches = Objects.equals(existingAuditDate, parsedReport.getAuditDate());
        boolean versionMatches = normalizeText(report.getVersion()).equals(normalizeText(parsedReport.getReportVersion()));
        boolean siteMatches = normalizeText(existingSiteName).equals(normalizeText(parsedReport.getSiteName()));
        boolean hostnameMatches = hasBoth(report.getHostname(), parsedReport.getHostname())
                && normalizeText(report.getHostname()).equals(normalizeText(parsedReport.getHostname()));
        boolean ipMatches = hasBoth(report.getIpAddress(), parsedReport.getIpAddress())
                && normalizeText(report.getIpAddress()).equals(normalizeText(parsedReport.getIpAddress()));

        return dateMatches && versionMatches && siteMatches
                && (!StringUtils.hasText(parsedReport.getHostname()) || hostnameMatches)
                && (!StringUtils.hasText(parsedReport.getIpAddress()) || ipMatches);
    }

    private Site createOrUpdateSite(ParsedAuditReport parsedReport) {
        Site site = siteRepository.findByNameIgnoreCase(parsedReport.getSiteName()).orElseGet(Site::new);
        site.setName(parsedReport.getSiteName());
        site.setCountry(firstNonBlank(parsedReport.getCountry(), site.getCountry()));
        site.setFirewallVendor(firstNonBlank(parsedReport.getVendor(), site.getFirewallVendor()));
        site.setFirewallModel(firstNonBlank(parsedReport.getDeviceModel(), site.getFirewallModel()));
        site.setLastAuditDate(parsedReport.getAuditDate());
        Integer securityScore = calculateImportedSecurityScore(parsedReport);
        if (securityScore != null) {
            site.setSecurityScore(securityScore);
        }
        if (site.getStatus() == null) {
            site.setStatus(SiteStatus.ACTIVE);
        }
        return siteRepository.save(site);
    }

    private Audit createAudit(Site site, ParsedAuditReport parsedReport) {
        Audit audit = new Audit();
        audit.setSite(site);
        audit.setAuditRound(nextAuditRound(site));
        audit.setAuditDate(parsedReport.getAuditDate());
        audit.setAuditor(parsedReport.getAuditor());
        audit.setOverallScore(calculateImportedSecurityScore(parsedReport));
        audit.setCompletionPercentage(complianceCompletion(parsedReport));
        audit.setRemarks("Imported from uploaded firewall audit report " + parsedReport.getReportVersion());
        return auditRepository.save(audit);
    }

    private Report createReport(Audit audit, ParsedAuditReport parsedReport, Path finalPath, String originalFileName) {
        Report report = new Report();
        report.setAudit(audit);
        report.setFileName(originalFileName);
        report.setFilePath(finalPath.toString());
        report.setVersion(parsedReport.getReportVersion());
        report.setUploadDate(LocalDate.now());
        report.setUploadedBy(parsedReport.getAuditor());
        report.setSiteName(parsedReport.getSiteName());
        report.setCountry(parsedReport.getCountry());
        report.setDeviceType(parsedReport.getDeviceType());
        report.setDeviceModel(parsedReport.getDeviceModel());
        report.setHostname(parsedReport.getHostname());
        report.setIpAddress(parsedReport.getIpAddress());
        report.setVendor(parsedReport.getVendor());
        report.setAuditDate(parsedReport.getAuditDate());
        report.setAuditor(parsedReport.getAuditor());
        report.setAssessmentDate(parsedReport.getAssessmentDate());
        report.setRiskScore(parsedReport.getRiskScore());
        report.setPassedComplianceCount(parsedReport.getPassedComplianceCount());
        report.setFailedComplianceCount(parsedReport.getFailedComplianceCount());
        report.setCriticalCount(parsedReport.getCriticalCount());
        report.setHighCount(parsedReport.getHighCount());
        report.setMediumCount(parsedReport.getMediumCount());
        report.setLowCount(parsedReport.getLowCount());
        report.setInfoCount(parsedReport.getInfoCount());
        return reportRepository.save(report);
    }

    private Integer nextAuditRound(Site site) {
        return auditRepository.findBySiteId(site.getId()).stream()
                .map(Audit::getAuditRound)
                .filter(round -> round != null && round > 0)
                .max(Comparator.naturalOrder())
                .orElse(0) + 1;
    }

    private Integer complianceCompletion(ParsedAuditReport parsedReport) {
        int passed = parsedReport.getPassedComplianceCount() != null ? parsedReport.getPassedComplianceCount() : 0;
        int failed = parsedReport.getFailedComplianceCount() != null ? parsedReport.getFailedComplianceCount() : 0;
        int total = passed + failed;
        return total == 0 ? 0 : (int) Math.round((passed * 100.0) / total);
    }

    private Integer scoreOrDefault(Integer score, Integer fallback) {
        if (score == null) {
            return fallback != null ? fallback : 0;
        }
        return Math.max(0, Math.min(100, score));
    }

    private Integer calculateImportedSecurityScore(ParsedAuditReport parsedReport) {
        Double riskScore = parsedReport.getRiskScoreValue() != null
                ? parsedReport.getRiskScoreValue()
                : parsedReport.getRiskScore() != null ? parsedReport.getRiskScore().doubleValue() : null;
        if (riskScore != null) {
            return scoreOrDefault((int) Math.round(100 - riskScore), null);
        }

        int passed = parsedReport.getPassedComplianceCount() != null ? parsedReport.getPassedComplianceCount() : 0;
        int failed = parsedReport.getFailedComplianceCount() != null ? parsedReport.getFailedComplianceCount() : 0;
        int total = passed + failed;
        return total > 0 ? scoreOrDefault((int) Math.round((passed * 100.0) / total), null) : null;
    }

    private String uniqueFileName(String originalFileName) {
        return UUID.randomUUID() + "-" + sanitizeFileName(originalFileName);
    }

    private String sanitizeFileName(String originalFileName) {
        String fileName = originalFileName != null ? originalFileName : "audit-report.pdf";
        return fileName.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    private String fileBaseName(String originalFileName) {
        String fileName = sanitizeFileName(originalFileName);
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(0, dot) : fileName;
    }

    private String firstNonBlank(String first, String second) {
        return StringUtils.hasText(first) ? first : second;
    }

    private boolean hasBoth(String first, String second) {
        return StringUtils.hasText(first) && StringUtils.hasText(second);
    }

    private String normalizeText(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.toLowerCase(Locale.ENGLISH).replaceAll("[^a-z0-9.]+", "");
    }
}
