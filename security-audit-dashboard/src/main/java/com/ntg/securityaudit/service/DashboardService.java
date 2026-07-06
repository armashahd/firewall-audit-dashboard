package com.ntg.securityaudit.service;

import com.ntg.securityaudit.dto.DashboardDTO;
import com.ntg.securityaudit.enums.FindingStatus;
import com.ntg.securityaudit.enums.Severity;
import com.ntg.securityaudit.entity.Audit;
import com.ntg.securityaudit.entity.Finding;
import com.ntg.securityaudit.entity.Site;
import com.ntg.securityaudit.repository.AuditRepository;
import com.ntg.securityaudit.repository.FindingRepository;
import com.ntg.securityaudit.repository.SiteRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private static final DateTimeFormatter LABEL_FORMAT = DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH);
    private static final List<String> AUDIT_AREAS = List.of(
            "Authentication",
            "Firewall Configuration",
            "Network Security",
            "Logging & Monitoring",
            "Patch Management",
            "Backup & Recovery"
    );

    private final SiteRepository siteRepository;
    private final AuditRepository auditRepository;
    private final FindingRepository findingRepository;
    private final DatabaseRepairService databaseRepairService;

    public DashboardService(SiteRepository siteRepository,
                            AuditRepository auditRepository,
                            FindingRepository findingRepository,
                            DatabaseRepairService databaseRepairService) {
        this.siteRepository = siteRepository;
        this.auditRepository = auditRepository;
        this.findingRepository = findingRepository;
        this.databaseRepairService = databaseRepairService;
    }

    public DashboardDTO getDashboardData() {
        databaseRepairService.repairIfNeeded();
        DashboardDTO dto = new DashboardDTO();

        List<Site> sites = siteRepository.findAll();
        List<Audit> audits = auditRepository.findAll();
        List<Finding> findings = findingRepository.findAll();

        long totalSites = sites.size();
        long totalAudits = audits.size();
        long totalFindings = findings.size();

        long openFindings = findings.stream()
                .filter(finding -> finding.getStatus() == FindingStatus.OPEN
                        || finding.getStatus() == FindingStatus.IN_PROGRESS
                        || finding.getStatus() == FindingStatus.ACCEPTED_RISK)
                .count();

        long closedFindings = findings.stream()
                .filter(finding -> finding.getStatus() == FindingStatus.CLOSED)
                .count();

        long criticalFindings = countFindingsBySeverity(findings, Severity.CRITICAL);
        long highFindings = countFindingsBySeverity(findings, Severity.HIGH);
        long mediumFindings = countFindingsBySeverity(findings, Severity.MEDIUM);
        long lowFindings = countFindingsBySeverity(findings, Severity.LOW);

        Double averageSiteScore = sites.stream()
                .map(Site::getSecurityScore)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0);

        Double averageAuditScore = audits.stream()
                .map(Audit::getOverallScore)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0);

        double overallSecurityScoreValue = averageSiteScore > 0 ? averageSiteScore : averageAuditScore;
        int overallSecurityScore = (int) Math.round(overallSecurityScoreValue);

        dto.setOverallSecurityScore(overallSecurityScore);
        dto.setCurrentRiskLevel(resolveCurrentRiskLevel(overallSecurityScore, criticalFindings, highFindings, mediumFindings));
        dto.setTotalSites(totalSites);
        dto.setTotalAudits(totalAudits);
        dto.setTotalFindings(totalFindings);
        dto.setOpenFindings(openFindings);
        dto.setClosedFindings(closedFindings);
        dto.setCompletedImprovements(closedFindings);
        dto.setImprovementCompletionPercentage(totalFindings == 0 ? 0 : (int) Math.round((closedFindings * 100.0) / totalFindings));
        dto.setNextScheduledAudit(calculateNextScheduledAudit(audits));
        dto.setAverageSiteScore((int) Math.round(averageSiteScore));
        dto.setAverageAuditScore((int) Math.round(averageAuditScore));
        dto.setCriticalFindings(criticalFindings);
        dto.setHighFindings(highFindings);
        dto.setMediumFindings(mediumFindings);
        dto.setLowFindings(lowFindings);
        dto.setCompletionPercentage((int) Math.round(averageAuditCompletionPercentage(audits)));

        List<Audit> auditsSortedByDate = audits.stream()
                .filter(audit -> audit.getAuditDate() != null)
                .sorted(Comparator.comparing(Audit::getAuditDate)
                        .thenComparing(audit -> audit.getAuditRound() != null ? audit.getAuditRound() : 0)
                        .thenComparing(audit -> audit.getId() != null ? audit.getId() : 0L))
                .toList();

        dto.setSecurityScoreTrendLabels(auditsSortedByDate.stream()
                .map(this::formatAuditLabel)
                .toList());
        dto.setSecurityScoreTrendData(auditsSortedByDate.stream()
                .map(audit -> audit.getOverallScore() == null ? 0 : audit.getOverallScore())
                .toList());
        dto.setAuditProgressLabels(auditsSortedByDate.stream()
                .map(this::formatAuditLabel)
                .toList());
        dto.setAuditProgressData(auditsSortedByDate.stream()
                .map(audit -> audit.getCompletionPercentage() == null ? 0 : audit.getCompletionPercentage())
                .toList());

        Map<String, Long> severityCounts = new LinkedHashMap<>();
        severityCounts.put("Critical", countFindingsBySeverity(findings, Severity.CRITICAL));
        severityCounts.put("High", countFindingsBySeverity(findings, Severity.HIGH));
        severityCounts.put("Medium", countFindingsBySeverity(findings, Severity.MEDIUM));
        severityCounts.put("Low", countFindingsBySeverity(findings, Severity.LOW));
        dto.setFindingsSeverityLabels(new ArrayList<>(severityCounts.keySet()));
        dto.setFindingsSeverityData(new ArrayList<>(severityCounts.values()));

        Map<String, Long> statusCounts = new LinkedHashMap<>();
        statusCounts.put("Open / In Progress", findings.stream()
                .filter(finding -> finding.getStatus() == FindingStatus.OPEN
                        || finding.getStatus() == FindingStatus.IN_PROGRESS
                        || finding.getStatus() == FindingStatus.ACCEPTED_RISK)
                .count());
        statusCounts.put("Closed", findings.stream()
                .filter(finding -> finding.getStatus() == FindingStatus.CLOSED)
                .count());
        dto.setOpenClosedLabels(new ArrayList<>(statusCounts.keySet()));
        dto.setOpenClosedData(new ArrayList<>(statusCounts.values()));

        Map<String, Long> categoryCounts = findings.stream()
                .collect(Collectors.groupingBy(this::normalizeCategory, Collectors.counting()));
        Map<String, Long> sortedCategoryCounts = categoryCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (left, right) -> left, LinkedHashMap::new));
        dto.setRiskDistributionLabels(new ArrayList<>(sortedCategoryCounts.keySet()));
        dto.setRiskDistributionData(new ArrayList<>(sortedCategoryCounts.values()));

        dto.setAuditAreaLabels(new ArrayList<>(AUDIT_AREAS));
        dto.setAuditAreaData(AUDIT_AREAS.stream()
                .map(category -> calculateAuditAreaScore(findings, category))
                .toList());

        dto.setTopRisks(buildTopRisks(findings));
        dto.setRecentAuditActivity(buildRecentAuditActivity(audits));
        dto.setUpcomingActionPlans(buildUpcomingActionPlans(findings, audits));

        return dto;
    }

    private long countFindingsBySeverity(List<Finding> findings, Severity severity) {
        return findings.stream()
                .filter(finding -> finding.getSeverity() == severity)
                .count();
    }

    private String resolveCurrentRiskLevel(int overallSecurityScore, long criticalFindings, long highFindings, long mediumFindings) {
        if (overallSecurityScore < 40 || criticalFindings > 0) {
            return "Critical";
        }
        if (overallSecurityScore < 60 || highFindings >= 6) {
            return "High";
        }
        if (overallSecurityScore < 80 || mediumFindings >= 10) {
            return "Medium";
        }
        return "Low";
    }

    private LocalDate calculateNextScheduledAudit(List<Audit> audits) {
        return audits.stream()
                .filter(audit -> audit.getSite() != null && audit.getAuditDate() != null)
                .collect(Collectors.groupingBy(audit -> audit.getSite().getId(), Collectors.mapping(Audit::getAuditDate, Collectors.maxBy(Comparator.naturalOrder()))))
                .values()
                .stream()
                .flatMap(optionalDate -> optionalDate.stream().map(date -> date.plusDays(90)))
                .sorted()
                .findFirst()
                .orElse(null);
    }

    private double averageAuditCompletionPercentage(List<Audit> audits) {
        return audits.stream()
                .map(Audit::getCompletionPercentage)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0);
    }

    private String formatAuditLabel(Audit audit) {
        String siteName = audit.getSite() != null && audit.getSite().getName() != null
                ? audit.getSite().getName()
                : "Audit";
        String roundLabel = audit.getAuditRound() != null ? " R" + audit.getAuditRound() : "";
        String dateLabel = audit.getAuditDate() != null ? " " + audit.getAuditDate().format(LABEL_FORMAT) : "";
        return siteName + roundLabel + dateLabel;
    }

    private String normalizeCategory(Finding finding) {
        if (finding.getCategory() == null || finding.getCategory().isBlank()) {
            return "Uncategorised";
        }
        return finding.getCategory().trim();
    }

    private int calculateAuditAreaScore(List<Finding> findings, String category) {
        long critical = findings.stream()
                .filter(finding -> category.equalsIgnoreCase(normalizeCategory(finding)))
                .filter(finding -> finding.getSeverity() == Severity.CRITICAL)
                .count();
        long high = findings.stream()
                .filter(finding -> category.equalsIgnoreCase(normalizeCategory(finding)))
                .filter(finding -> finding.getSeverity() == Severity.HIGH)
                .count();
        long medium = findings.stream()
                .filter(finding -> category.equalsIgnoreCase(normalizeCategory(finding)))
                .filter(finding -> finding.getSeverity() == Severity.MEDIUM)
                .count();
        long low = findings.stream()
                .filter(finding -> category.equalsIgnoreCase(normalizeCategory(finding)))
                .filter(finding -> finding.getSeverity() == Severity.LOW)
                .count();

        int score = 100;
        score -= critical * 22;
        score -= high * 14;
        score -= medium * 8;
        score -= low * 4;
        return Math.max(0, score);
    }

    private List<DashboardDTO.RiskItem> buildTopRisks(List<Finding> findings) {
        Map<String, List<Finding>> groupedFindings = findings.stream()
                .collect(Collectors.groupingBy(finding -> {
                    String title = finding.getTitle();
                    return title == null || title.isBlank() ? "Unnamed Risk" : title.trim();
                }, LinkedHashMap::new, Collectors.toList()));

        return groupedFindings.entrySet().stream()
                .map(entry -> {
                    DashboardDTO.RiskItem item = new DashboardDTO.RiskItem();
                    item.setTitle(entry.getKey());
                    item.setCount((long) entry.getValue().size());
                    item.setCategory(entry.getValue().stream()
                            .map(this::normalizeCategory)
                            .filter(Objects::nonNull)
                            .findFirst()
                            .orElse("Uncategorised"));
                    item.setSeverity(entry.getValue().stream()
                            .map(Finding::getSeverity)
                            .filter(Objects::nonNull)
                            .max(Comparator.comparingInt(this::severityRank))
                            .map(Enum::name)
                            .orElse("LOW"));
                    return item;
                })
                .sorted(Comparator.comparing(DashboardDTO.RiskItem::getCount, Comparator.reverseOrder())
                        .thenComparing(item -> severityRank(item.getSeverity()), Comparator.reverseOrder()))
                .limit(5)
                .toList();
    }

    private int severityRank(Severity severity) {
        if (severity == null) {
            return 0;
        }
        return severityRank(severity.name());
    }

    private int severityRank(String severity) {
        if (severity == null) {
            return 0;
        }
        return switch (severity.toUpperCase(Locale.ENGLISH)) {
            case "CRITICAL" -> 4;
            case "HIGH" -> 3;
            case "MEDIUM" -> 2;
            case "LOW" -> 1;
            default -> 0;
        };
    }

    private List<DashboardDTO.AuditActivityItem> buildRecentAuditActivity(List<Audit> audits) {
        return audits.stream()
                .sorted(Comparator.comparing(Audit::getAuditDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .reversed()
                        .thenComparing(audit -> audit.getAuditRound() != null ? audit.getAuditRound() : 0, Comparator.reverseOrder())
                        .thenComparing(audit -> audit.getId() != null ? audit.getId() : 0L, Comparator.reverseOrder()))
                .limit(5)
                .map(audit -> {
                    DashboardDTO.AuditActivityItem item = new DashboardDTO.AuditActivityItem();
                    item.setAuditId(audit.getId());
                    item.setSiteName(audit.getSite() != null ? audit.getSite().getName() : "-");
                    item.setRoundNumber(audit.getAuditRound());
                    item.setAuditDate(audit.getAuditDate());
                    item.setOverallScore(audit.getOverallScore());
                    item.setCompletionPercentage(audit.getCompletionPercentage());
                    item.setAuditor(audit.getAuditor());
                    return item;
                })
                .toList();
    }

    private List<DashboardDTO.ActionPlanItem> buildUpcomingActionPlans(List<Finding> findings, List<Audit> audits) {
        Map<Long, Audit> auditMap = audits.stream()
                .filter(audit -> audit.getId() != null)
                .collect(Collectors.toMap(Audit::getId, audit -> audit, (left, right) -> left, LinkedHashMap::new));

        return findings.stream()
                .filter(finding -> finding.getStatus() != FindingStatus.CLOSED)
                .sorted(Comparator.comparing(Finding::getDueDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(finding -> finding.getSeverity() != null ? severityRank(finding.getSeverity()) : 0, Comparator.reverseOrder())
                        .thenComparing(finding -> finding.getId() != null ? finding.getId() : 0L, Comparator.reverseOrder()))
                .limit(5)
                .map(finding -> {
                    DashboardDTO.ActionPlanItem item = new DashboardDTO.ActionPlanItem();
                    item.setFindingId(finding.getId());
                    item.setAction(finding.getTitle());
                    item.setDescription(finding.getRecommendation() != null ? finding.getRecommendation() : finding.getDescription());
                    item.setOwner(finding.getAssignedTo());
                    item.setPriority(finding.getSeverity() != null ? finding.getSeverity().name() : "LOW");
                    item.setTargetDate(finding.getDueDate());
                    item.setStatus(finding.getStatus() != null ? finding.getStatus().name().replace('_', ' ') : "OPEN");
                    Audit linkedAudit = finding.getAudit() != null ? auditMap.get(finding.getAudit().getId()) : null;
                    item.setNotes(linkedAudit != null && linkedAudit.getSite() != null
                            ? linkedAudit.getSite().getName() + " / Audit #" + linkedAudit.getId()
                            : finding.getCategory());
                    return item;
                })
                .toList();
    }
}
