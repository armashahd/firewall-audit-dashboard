package com.ntg.securityaudit.service;

import com.ntg.securityaudit.dto.DashboardDTO;
import com.ntg.securityaudit.dto.DashboardFilter;
import com.ntg.securityaudit.entity.Audit;
import com.ntg.securityaudit.entity.AuditException;
import com.ntg.securityaudit.entity.Finding;
import com.ntg.securityaudit.entity.Site;
import com.ntg.securityaudit.enums.AuditExceptionStatus;
import com.ntg.securityaudit.enums.FindingStatus;
import com.ntg.securityaudit.enums.Severity;
import com.ntg.securityaudit.repository.AuditExceptionRepository;
import com.ntg.securityaudit.repository.AuditRepository;
import com.ntg.securityaudit.repository.FindingRepository;
import com.ntg.securityaudit.repository.SiteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private static final DateTimeFormatter LABEL_FORMAT = DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH);

    private final SiteRepository siteRepository;
    private final AuditRepository auditRepository;
    private final FindingRepository findingRepository;
    private final AuditExceptionRepository auditExceptionRepository;
    private final DatabaseRepairService databaseRepairService;
    private final DashboardStatisticsService dashboardStatisticsService;

    public DashboardService(SiteRepository siteRepository,
                            AuditRepository auditRepository,
                            FindingRepository findingRepository,
                            AuditExceptionRepository auditExceptionRepository,
                            DatabaseRepairService databaseRepairService,
                            DashboardStatisticsService dashboardStatisticsService) {
        this.siteRepository = siteRepository;
        this.auditRepository = auditRepository;
        this.findingRepository = findingRepository;
        this.auditExceptionRepository = auditExceptionRepository;
        this.databaseRepairService = databaseRepairService;
        this.dashboardStatisticsService = dashboardStatisticsService;
    }

    public DashboardDTO getDashboardData() {
        return getDashboardData(new DashboardFilter());
    }

    @Transactional
    public DashboardDTO getDashboardData(DashboardFilter filter) {
        databaseRepairService.repairIfNeeded();
        normalizeLiveStatuses();

        DashboardDTO dto = new DashboardDTO();
        DashboardFilter safeFilter = filter != null ? filter : new DashboardFilter();

        List<Site> allSites = siteRepository.findAll();
        List<Audit> allAudits = auditRepository.findAll();
        List<Finding> allFindings = findingRepository.findAll();
        List<AuditException> allExceptions = auditExceptionRepository.findAll();

        dto.setCategoryOptions(allFindings.stream()
                .map(this::normalizeCategory)
                .filter(category -> !"Uncategorised".equals(category))
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList());
        dto.setAuditRoundOptions(allAudits.stream()
                .map(Audit::getAuditRound)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList());
        dto.setYearOptions(allAudits.stream()
                .map(Audit::getAuditDate)
                .filter(Objects::nonNull)
                .map(LocalDate::getYear)
                .distinct()
                .sorted(Comparator.reverseOrder())
                .toList());

        List<Audit> audits = allAudits.stream()
                .filter(audit -> matchesAuditFilter(audit, safeFilter))
                .toList();
        Set<Long> auditIds = audits.stream()
                .map(Audit::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        List<Site> sites = allSites.stream()
                .filter(site -> safeFilter.getSiteId() == null || safeFilter.getSiteId().equals(site.getId()))
                .toList();
        List<Finding> findings = allFindings.stream()
                .filter(finding -> finding.getAudit() != null && finding.getAudit().getId() != null && auditIds.contains(finding.getAudit().getId()))
                .filter(finding -> matchesFindingFilter(finding, safeFilter))
                .toList();
        List<AuditException> exceptions = allExceptions.stream()
                .filter(exception -> matchesExceptionFilter(exception, auditIds, safeFilter))
                .toList();

        long totalFindings = findings.size();
        long openFindings = findings.stream().filter(this::isOpenOrInProgress).count();
        long closedFindings = findings.stream().filter(finding -> finding.getStatus() != null && finding.getStatus().isClosed()).count();
        long acceptedRiskFindings = findings.stream().filter(finding -> finding.getStatus() != null && finding.getStatus().isAcceptedRisk()).count();
        long overdueFindings = findings.stream().filter(this::isOverdue).count();
        long criticalFindings = countFindingsBySeverity(findings, Severity.CRITICAL);
        long highFindings = countFindingsBySeverity(findings, Severity.HIGH);
        long mediumFindings = countFindingsBySeverity(findings, Severity.MEDIUM);
        long lowFindings = countFindingsBySeverity(findings, Severity.LOW);

        double averageSiteScore = sites.stream()
                .map(Site::getSecurityScore)
                .filter(this::isUsableScore)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0);
        double averageAuditScore = audits.stream()
                .map(Audit::getOverallScore)
                .filter(this::isUsableScore)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0);
        int overallSecurityScore = (int) Math.round(averageAuditScore > 0 ? averageAuditScore : averageSiteScore);
        int completionPercentage = (int) Math.round(averageAuditCompletionPercentage(audits));

        dto.setOverallSecurityScore(overallSecurityScore);
        dto.setCurrentRiskLevel(dashboardStatisticsService.resolveRiskLevel(findings, overallSecurityScore));
        dto.setTotalSites((long) sites.size());
        dto.setTotalAudits((long) audits.size());
        dto.setTotalFindings(totalFindings);
        dto.setOpenFindings(openFindings);
        dto.setClosedFindings(closedFindings);
        dto.setAcceptedRiskFindings(acceptedRiskFindings);
        dto.setOverdueFindings(overdueFindings);
        dto.setCompletedImprovements(closedFindings);
        dto.setImprovementCompletionPercentage(totalFindings == 0 ? 0 : (int) Math.round((closedFindings * 100.0) / totalFindings));
        dto.setTotalActiveExceptions(countExceptionsByStatus(exceptions, AuditExceptionStatus.ACTIVE));
        dto.setTotalExpiredExceptions(countExceptionsByStatus(exceptions, AuditExceptionStatus.EXPIRED));
        dto.setNextScheduledAudit(calculateNextScheduledAudit(audits));
        dto.setAverageSiteScore((int) Math.round(averageSiteScore));
        dto.setAverageAuditScore((int) Math.round(averageAuditScore));
        dto.setCriticalFindings(criticalFindings);
        dto.setHighFindings(highFindings);
        dto.setMediumFindings(mediumFindings);
        dto.setLowFindings(lowFindings);
        dto.setCompletionPercentage(completionPercentage);

        buildTrends(dto, audits, allFindings);
        buildCharts(dto, audits, findings);
        dto.setTopRisks(buildTopRisks(findings));
        dto.setRecentAuditActivity(buildRecentAuditActivity(audits));
        dto.setUpcomingActionPlans(buildUpcomingActionPlans(findings, audits));
        dto.setUpcomingExceptionExpiries(buildUpcomingExceptionExpiries(exceptions));
        dto.setSiteHeatmap(buildSiteHeatmap(sites, audits, findings));
        dto.setActivityFeed(buildActivityFeed(audits, findings, exceptions));

        return dto;
    }

    private void normalizeLiveStatuses() {
        LocalDate today = LocalDate.now();
        List<Finding> changedFindings = findingRepository.findAll().stream()
                .filter(finding -> finding.getClosedDate() != null && finding.getStatus() != FindingStatus.CLOSED)
                .peek(finding -> finding.setStatus(FindingStatus.CLOSED))
                .toList();
        if (!changedFindings.isEmpty()) {
            findingRepository.saveAll(changedFindings);
        }

        List<AuditException> changedExceptions = auditExceptionRepository.findAll().stream()
                .filter(exception -> exception.getStatus() != AuditExceptionStatus.CLOSED)
                .filter(exception -> exception.getExpiryDate() != null)
                .filter(exception -> effectiveExceptionStatus(exception) != exception.getStatus())
                .peek(exception -> exception.setStatus(effectiveExceptionStatus(exception)))
                .toList();
        if (!changedExceptions.isEmpty()) {
            auditExceptionRepository.saveAll(changedExceptions);
        }
    }

    private boolean matchesAuditFilter(Audit audit, DashboardFilter filter) {
        if (filter.getSiteId() != null && (audit.getSite() == null || !filter.getSiteId().equals(audit.getSite().getId()))) {
            return false;
        }
        if (filter.getAuditRound() != null && !filter.getAuditRound().equals(audit.getAuditRound())) {
            return false;
        }
        return filter.getYear() == null || audit.getAuditDate() != null && filter.getYear().equals(audit.getAuditDate().getYear());
    }

    private boolean matchesFindingFilter(Finding finding, DashboardFilter filter) {
        if (filter.getCategory() != null && !filter.getCategory().isBlank() && !filter.getCategory().equalsIgnoreCase(normalizeCategory(finding))) {
            return false;
        }
        if (filter.getSeverity() != null && finding.getSeverity() != filter.getSeverity()) {
            return false;
        }
        return filter.getStatus() == null || finding.getStatus() == filter.getStatus();
    }

    private boolean matchesExceptionFilter(AuditException exception, Set<Long> auditIds, DashboardFilter filter) {
        if (exception.getRelatedAudit() != null && exception.getRelatedAudit().getId() != null && !auditIds.contains(exception.getRelatedAudit().getId())) {
            return false;
        }
        if (filter.getSiteId() != null && (exception.getRelatedSite() == null || !filter.getSiteId().equals(exception.getRelatedSite().getId()))) {
            return false;
        }
        return filter.getYear() == null || exception.getApprovalDate() != null && filter.getYear().equals(exception.getApprovalDate().getYear());
    }

    private void buildCharts(DashboardDTO dto, List<Audit> audits, List<Finding> findings) {
        List<Audit> auditsSortedByDate = audits.stream()
                .filter(audit -> audit.getAuditDate() != null)
                .sorted(Comparator.comparing(Audit::getAuditDate)
                        .thenComparing(audit -> audit.getAuditRound() != null ? audit.getAuditRound() : 0)
                        .thenComparing(audit -> audit.getId() != null ? audit.getId() : 0L))
                .toList();

        dto.setSecurityScoreTrendLabels(auditsSortedByDate.stream().map(this::formatAuditLabel).toList());
        dto.setSecurityScoreTrendData(auditsSortedByDate.stream().map(audit -> audit.getOverallScore() == null ? 0 : audit.getOverallScore()).toList());
        dto.setAuditProgressLabels(auditsSortedByDate.stream().map(this::formatAuditLabel).toList());
        dto.setAuditProgressData(auditsSortedByDate.stream().map(audit -> audit.getCompletionPercentage() == null ? 0 : audit.getCompletionPercentage()).toList());

        Map<String, Long> severityCounts = new LinkedHashMap<>();
        severityCounts.put("Critical", countFindingsBySeverity(findings, Severity.CRITICAL));
        severityCounts.put("High", countFindingsBySeverity(findings, Severity.HIGH));
        severityCounts.put("Medium", countFindingsBySeverity(findings, Severity.MEDIUM));
        severityCounts.put("Low", countFindingsBySeverity(findings, Severity.LOW));
        dto.setFindingsSeverityLabels(new ArrayList<>(severityCounts.keySet()));
        dto.setFindingsSeverityData(new ArrayList<>(severityCounts.values()));

        dto.setOpenClosedLabels(List.of("Open / In Progress", "Closed", "Accepted Risk"));
        dto.setOpenClosedData(List.of(
                findings.stream().filter(this::isOpenOrInProgress).count(),
                findings.stream().filter(finding -> finding.getStatus() != null && finding.getStatus().isClosed()).count(),
                findings.stream().filter(finding -> finding.getStatus() != null && finding.getStatus().isAcceptedRisk()).count()
        ));

        Map<String, Long> categoryCounts = findings.stream()
                .collect(Collectors.groupingBy(this::normalizeCategory, Collectors.counting()));
        Map<String, Long> sortedCategoryCounts = categoryCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (left, right) -> left, LinkedHashMap::new));
        dto.setRiskDistributionLabels(new ArrayList<>(sortedCategoryCounts.keySet()));
        dto.setRiskDistributionData(new ArrayList<>(sortedCategoryCounts.values()));

        dto.setAuditAreaLabels(new ArrayList<>(sortedCategoryCounts.keySet()));
        dto.setAuditAreaData(sortedCategoryCounts.keySet().stream()
                .map(category -> calculateAuditAreaScore(findings, category))
                .toList());
    }

    private void buildTrends(DashboardDTO dto, List<Audit> audits, List<Finding> allFindings) {
        List<Audit> sorted = audits.stream()
                .sorted(Comparator.comparing(Audit::getAuditDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(audit -> audit.getAuditRound() != null ? audit.getAuditRound() : 0)
                        .thenComparing(audit -> audit.getId() != null ? audit.getId() : 0L))
                .toList();
        if (sorted.size() < 2) {
            setFlatTrends(dto);
            return;
        }

        Audit previous = sorted.get(sorted.size() - 2);
        Audit current = sorted.get(sorted.size() - 1);
        List<Finding> currentFindings = findingsForAudit(allFindings, current);
        List<Finding> previousFindings = findingsForAudit(allFindings, previous);

        int currentScore = current.getOverallScore() == null ? 0 : current.getOverallScore();
        int previousScore = previous.getOverallScore() == null ? 0 : previous.getOverallScore();
        int currentCompletion = current.getCompletionPercentage() == null ? 0 : current.getCompletionPercentage();
        int previousCompletion = previous.getCompletionPercentage() == null ? 0 : previous.getCompletionPercentage();

        setDirectionalTrend(dto::setScoreTrendLabel, dto::setScoreTrendClass, currentScore - previousScore, "% since previous audit");
        setOpenFindingsTrend(dto, (int) (currentFindings.stream().filter(this::isOpenOrInProgress).count() - previousFindings.stream().filter(this::isOpenOrInProgress).count()));
        setClosedFindingsTrend(dto, (int) (currentFindings.stream().filter(finding -> finding.getStatus() != null && finding.getStatus().isClosed()).count()
                - previousFindings.stream().filter(finding -> finding.getStatus() != null && finding.getStatus().isClosed()).count()));
        setDirectionalTrend(dto::setCompletionTrendLabel, dto::setCompletionTrendClass, currentCompletion - previousCompletion, "% since previous audit");
    }

    private void setFlatTrends(DashboardDTO dto) {
        dto.setScoreTrendLabel("No previous audit");
        dto.setScoreTrendClass("text-muted");
        dto.setOpenFindingsTrendLabel("No previous audit");
        dto.setOpenFindingsTrendClass("text-muted");
        dto.setClosedFindingsTrendLabel("No previous audit");
        dto.setClosedFindingsTrendClass("text-muted");
        dto.setCompletionTrendLabel("No previous audit");
        dto.setCompletionTrendClass("text-muted");
    }

    private void setDirectionalTrend(java.util.function.Consumer<String> labelSetter,
                                     java.util.function.Consumer<String> classSetter,
                                     int delta,
                                     String suffix) {
        if (delta == 0) {
            labelSetter.accept("no change");
            classSetter.accept("text-muted");
            return;
        }
        labelSetter.accept((delta > 0 ? "up " : "down ") + Math.abs(delta) + suffix);
        classSetter.accept(delta > 0 ? "text-success" : "text-danger");
    }

    private void setOpenFindingsTrend(DashboardDTO dto, int delta) {
        if (delta == 0) {
            dto.setOpenFindingsTrendLabel("no change");
            dto.setOpenFindingsTrendClass("text-muted");
            return;
        }
        dto.setOpenFindingsTrendLabel((delta > 0 ? "worsened +" : "improved ") + delta + " since previous audit");
        dto.setOpenFindingsTrendClass(delta > 0 ? "text-danger" : "text-success");
    }

    private void setClosedFindingsTrend(DashboardDTO dto, int delta) {
        if (delta == 0) {
            dto.setClosedFindingsTrendLabel("no change");
            dto.setClosedFindingsTrendClass("text-muted");
            return;
        }
        dto.setClosedFindingsTrendLabel((delta > 0 ? "improved +" : "worsened ") + delta + " since previous audit");
        dto.setClosedFindingsTrendClass(delta > 0 ? "text-success" : "text-danger");
    }

    private List<Finding> findingsForAudit(List<Finding> findings, Audit audit) {
        return findings.stream()
                .filter(finding -> finding.getAudit() != null && audit.getId() != null && audit.getId().equals(finding.getAudit().getId()))
                .toList();
    }

    private List<DashboardDTO.SiteHeatmapItem> buildSiteHeatmap(List<Site> sites, List<Audit> audits, List<Finding> findings) {
        return sites.stream()
                .map(site -> {
                    List<Audit> siteAudits = audits.stream()
                            .filter(audit -> audit.getSite() != null && site.getId() != null && site.getId().equals(audit.getSite().getId()))
                            .toList();
                    Audit latestAudit = siteAudits.stream()
                            .max(Comparator.comparing(Audit::getAuditDate, Comparator.nullsLast(Comparator.naturalOrder()))
                                    .thenComparing(audit -> audit.getId() != null ? audit.getId() : 0L))
                            .orElse(null);
                    Set<Long> siteAuditIds = siteAudits.stream().map(Audit::getId).collect(Collectors.toSet());
                    List<Finding> siteFindings = findings.stream()
                            .filter(finding -> finding.getAudit() != null && siteAuditIds.contains(finding.getAudit().getId()))
                            .toList();

                    long open = siteFindings.stream().filter(this::isOpenOrInProgress).count();
                    long critical = siteFindings.stream().filter(finding -> finding.getSeverity() == Severity.CRITICAL && isOpenOrInProgress(finding)).count();
                    long overdue = siteFindings.stream().filter(this::isOverdue).count();
                    int score = latestAudit != null && latestAudit.getOverallScore() != null ? latestAudit.getOverallScore() : 0;

                    DashboardDTO.SiteHeatmapItem item = new DashboardDTO.SiteHeatmapItem();
                    item.setSiteName(site.getName());
                    item.setLatestAuditScore(score);
                    item.setOpenFindings(open);
                    item.setCriticalFindings(critical);
                    item.setOverdueFindings(overdue);
                    item.setRiskLevel(dashboardStatisticsService.resolveRiskLevel(siteFindings, score));
                    return item;
                })
                .toList();
    }

    private List<DashboardDTO.ActivityFeedItem> buildActivityFeed(List<Audit> audits, List<Finding> findings, List<AuditException> exceptions) {
        List<DashboardDTO.ActivityFeedItem> items = new ArrayList<>();
        audits.forEach(audit -> items.add(activity(audit.getAuditDate(), "Audit", "Audit completed for "
                + (audit.getSite() != null ? audit.getSite().getName() : "site")
                + (audit.getAuditRound() != null ? " round " + audit.getAuditRound() : ""), "bg-primary",
                audit.getId() != null ? "/audits/details/" + audit.getId() : null)));
        findings.forEach(finding -> {
            if (finding.getStatus() == FindingStatus.CLOSED) {
                items.add(activity(finding.getClosedDate(), "Finding", "Finding closed: " + finding.getTitle(), "bg-success", findingUrl(finding)));
            } else if (finding.getStatus() == FindingStatus.ACCEPTED_RISK) {
                items.add(activity(finding.getDueDate(), "Finding", "Finding accepted as risk: " + finding.getTitle(), "bg-secondary", findingUrl(finding)));
            } else {
                items.add(activity(finding.getDueDate(), "Finding", "Finding created/open: " + finding.getTitle(), "bg-danger", findingUrl(finding)));
            }
        });
        exceptions.forEach(exception -> items.add(activity(exception.getExpiryDate(), "Exception",
                effectiveExceptionStatus(exception) == AuditExceptionStatus.EXPIRED
                        ? "Exception expired: " + exception.getExceptionName()
                        : "Exception active: " + exception.getExceptionName(),
                effectiveExceptionStatus(exception) == AuditExceptionStatus.EXPIRED ? "bg-warning text-dark" : "bg-success",
                exception.getId() != null ? "/audit-exceptions/details/" + exception.getId() : null)));

        return items.stream()
                .sorted(Comparator.comparing(DashboardDTO.ActivityFeedItem::getActivityDate, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .limit(10)
                .toList();
    }

    private DashboardDTO.ActivityFeedItem activity(LocalDate date, String type, String message, String badgeClass, String targetUrl) {
        DashboardDTO.ActivityFeedItem item = new DashboardDTO.ActivityFeedItem();
        item.setActivityDate(date);
        item.setType(type);
        item.setMessage(message);
        item.setBadgeClass(badgeClass);
        item.setTargetUrl(targetUrl);
        return item;
    }

    private String findingUrl(Finding finding) {
        return finding.getId() != null ? "/findings/details/" + finding.getId() : null;
    }

    private long countExceptionsByStatus(List<AuditException> exceptions, AuditExceptionStatus status) {
        return exceptions.stream().filter(exception -> effectiveExceptionStatus(exception) == status).count();
    }

    private long countFindingsBySeverity(List<Finding> findings, Severity severity) {
        return findings.stream().filter(finding -> finding.getSeverity() == severity).count();
    }

    private String resolveCurrentRiskLevel(List<Finding> findings, int averageSecurityScore) {
        if (findings.stream().anyMatch(finding -> finding.getSeverity() == Severity.CRITICAL && isOpenOrInProgress(finding))) {
            return "Critical";
        }
        if (findings.stream().anyMatch(finding -> finding.getSeverity() == Severity.HIGH && isOverdue(finding))) {
            return "High";
        }
        if (averageSecurityScore < 70) {
            return "High";
        }
        if (findings.stream().anyMatch(finding -> finding.getSeverity() == Severity.MEDIUM && isOpenOrInProgress(finding))) {
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

    private boolean isUsableScore(Integer score) {
        return score != null && score > 0;
    }

    private String formatAuditLabel(Audit audit) {
        String siteName = audit.getSite() != null && audit.getSite().getName() != null ? audit.getSite().getName() : "Audit";
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
        long critical = findings.stream().filter(finding -> category.equalsIgnoreCase(normalizeCategory(finding))).filter(finding -> finding.getSeverity() == Severity.CRITICAL).count();
        long high = findings.stream().filter(finding -> category.equalsIgnoreCase(normalizeCategory(finding))).filter(finding -> finding.getSeverity() == Severity.HIGH).count();
        long medium = findings.stream().filter(finding -> category.equalsIgnoreCase(normalizeCategory(finding))).filter(finding -> finding.getSeverity() == Severity.MEDIUM).count();
        long low = findings.stream().filter(finding -> category.equalsIgnoreCase(normalizeCategory(finding))).filter(finding -> finding.getSeverity() == Severity.LOW).count();
        return Math.max(0, 100 - (int) critical * 22 - (int) high * 14 - (int) medium * 8 - (int) low * 4);
    }

    private List<DashboardDTO.RiskItem> buildTopRisks(List<Finding> findings) {
        Map<String, List<Finding>> groupedFindings = findings.stream()
                .collect(Collectors.groupingBy(finding -> finding.getTitle() == null || finding.getTitle().isBlank() ? "Unnamed Risk" : finding.getTitle().trim(),
                        LinkedHashMap::new, Collectors.toList()));

        return groupedFindings.entrySet().stream()
                .map(entry -> {
                    Finding representativeFinding = entry.getValue().stream()
                            .max(Comparator.comparingInt((Finding finding) -> severityRank(finding.getSeverity()))
                                    .thenComparing(finding -> finding.getId() != null ? finding.getId() : 0L))
                            .orElse(null);
                    DashboardDTO.RiskItem item = new DashboardDTO.RiskItem();
                    item.setFindingId(representativeFinding != null ? representativeFinding.getId() : null);
                    item.setTitle(entry.getKey());
                    item.setCount((long) entry.getValue().size());
                    item.setCategory(entry.getValue().stream().map(this::normalizeCategory).findFirst().orElse("Uncategorised"));
                    item.setSiteName(representativeFinding != null
                            && representativeFinding.getAudit() != null
                            && representativeFinding.getAudit().getSite() != null
                            ? representativeFinding.getAudit().getSite().getName()
                            : "-");
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
                .filter(this::isOpenOrInProgress)
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
                    item.setStatus(finding.getStatus() != null ? finding.getStatus().getDisplayName() : "Open");
                    Audit linkedAudit = finding.getAudit() != null ? auditMap.get(finding.getAudit().getId()) : null;
                    item.setNotes(linkedAudit != null && linkedAudit.getSite() != null ? linkedAudit.getSite().getName() + " / Audit #" + linkedAudit.getId() : finding.getCategory());
                    return item;
                })
                .toList();
    }

    private List<DashboardDTO.ExceptionExpiryItem> buildUpcomingExceptionExpiries(List<AuditException> exceptions) {
        return exceptions.stream()
                .filter(exception -> effectiveExceptionStatus(exception) != AuditExceptionStatus.CLOSED)
                .filter(exception -> exception.getExpiryDate() != null)
                .filter(exception -> !exception.getExpiryDate().isBefore(LocalDate.now()))
                .sorted(Comparator.comparing(AuditException::getExpiryDate).thenComparing(exception -> exception.getId() != null ? exception.getId() : 0L))
                .limit(5)
                .map(exception -> {
                    DashboardDTO.ExceptionExpiryItem item = new DashboardDTO.ExceptionExpiryItem();
                    item.setExceptionId(exception.getId());
                    item.setExceptionName(exception.getExceptionName() != null ? exception.getExceptionName() : "-");
                    item.setSiteName(exception.getRelatedSite() != null ? exception.getRelatedSite().getName() : "-");
                    item.setAuditLabel(exception.getRelatedAudit() != null ? "Audit #" + exception.getRelatedAudit().getId() : "-");
                    item.setExpiryDate(exception.getExpiryDate());
                    item.setStatus(effectiveExceptionStatus(exception).name());
                    return item;
                })
                .toList();
    }

    private boolean isOpenOrInProgress(Finding finding) {
        return dashboardStatisticsService.isOpenOrInProgress(finding);
    }

    private boolean isOverdue(Finding finding) {
        return dashboardStatisticsService.isOverdue(finding);
    }

    private AuditExceptionStatus effectiveExceptionStatus(AuditException exception) {
        return dashboardStatisticsService.effectiveExceptionStatus(exception);
    }

    private int severityRank(Severity severity) {
        return severity == null ? 0 : severityRank(severity.name());
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
}
