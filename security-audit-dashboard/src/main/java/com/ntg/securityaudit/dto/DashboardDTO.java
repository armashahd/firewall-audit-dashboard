package com.ntg.securityaudit.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DashboardDTO {

    private Integer overallSecurityScore;
    private String currentRiskLevel;
    private Long totalSites;
    private Long totalAudits;
    private Long totalFindings;
    private Long openFindings;
    private Long closedFindings;
    private Long acceptedRiskFindings;
    private Long overdueFindings;
    private Long completedImprovements;
    private Integer improvementCompletionPercentage;
    private Long totalActiveExceptions;
    private Long totalExpiredExceptions;
    private LocalDate nextScheduledAudit;
    private Integer averageSiteScore;
    private Integer averageAuditScore;
    private Long criticalFindings;
    private Long highFindings;
    private Long mediumFindings;
    private Long lowFindings;
    private Integer completionPercentage;
    private String scoreTrendLabel;
    private String scoreTrendClass;
    private String openFindingsTrendLabel;
    private String openFindingsTrendClass;
    private String closedFindingsTrendLabel;
    private String closedFindingsTrendClass;
    private String completionTrendLabel;
    private String completionTrendClass;
    private List<String> categoryOptions = new ArrayList<>();
    private List<Integer> auditRoundOptions = new ArrayList<>();
    private List<Integer> yearOptions = new ArrayList<>();
    private List<String> securityScoreTrendLabels = new ArrayList<>();
    private List<Integer> securityScoreTrendData = new ArrayList<>();
    private List<String> riskDistributionLabels = new ArrayList<>();
    private List<Long> riskDistributionData = new ArrayList<>();
    private List<String> findingsSeverityLabels = new ArrayList<>();
    private List<Long> findingsSeverityData = new ArrayList<>();
    private List<String> openClosedLabels = new ArrayList<>();
    private List<Long> openClosedData = new ArrayList<>();
    private List<String> auditProgressLabels = new ArrayList<>();
    private List<Integer> auditProgressData = new ArrayList<>();
    private List<String> auditAreaLabels = new ArrayList<>();
    private List<Integer> auditAreaData = new ArrayList<>();
    private List<RiskItem> topRisks = new ArrayList<>();
    private List<AuditActivityItem> recentAuditActivity = new ArrayList<>();
    private List<ActionPlanItem> upcomingActionPlans = new ArrayList<>();
    private List<ExceptionExpiryItem> upcomingExceptionExpiries = new ArrayList<>();
    private List<SiteHeatmapItem> siteHeatmap = new ArrayList<>();
    private List<ActivityFeedItem> activityFeed = new ArrayList<>();
    private List<SummaryItem> findingTypeSummary = new ArrayList<>();
    private List<SummaryItem> complianceStatusSummary = new ArrayList<>();
    private List<SummaryItem> vulnerabilityStatusSummary = new ArrayList<>();
    private List<SummaryItem> vulnerabilitySeveritySummary = new ArrayList<>();

    public DashboardDTO() {
    }

    public Integer getOverallSecurityScore() {
        return overallSecurityScore;
    }

    public void setOverallSecurityScore(Integer overallSecurityScore) {
        this.overallSecurityScore = overallSecurityScore;
    }

    public String getCurrentRiskLevel() {
        return currentRiskLevel;
    }

    public void setCurrentRiskLevel(String currentRiskLevel) {
        this.currentRiskLevel = currentRiskLevel;
    }

    public Long getTotalSites() {
        return totalSites;
    }

    public void setTotalSites(Long totalSites) {
        this.totalSites = totalSites;
    }

    public Long getTotalAudits() {
        return totalAudits;
    }

    public void setTotalAudits(Long totalAudits) {
        this.totalAudits = totalAudits;
    }

    public Long getTotalFindings() {
        return totalFindings;
    }

    public void setTotalFindings(Long totalFindings) {
        this.totalFindings = totalFindings;
    }

    public Long getOpenFindings() {
        return openFindings;
    }

    public void setOpenFindings(Long openFindings) {
        this.openFindings = openFindings;
    }

    public Long getClosedFindings() {
        return closedFindings;
    }

    public void setClosedFindings(Long closedFindings) {
        this.closedFindings = closedFindings;
    }

    public Long getAcceptedRiskFindings() {
        return acceptedRiskFindings;
    }

    public void setAcceptedRiskFindings(Long acceptedRiskFindings) {
        this.acceptedRiskFindings = acceptedRiskFindings;
    }

    public Long getOverdueFindings() {
        return overdueFindings;
    }

    public void setOverdueFindings(Long overdueFindings) {
        this.overdueFindings = overdueFindings;
    }

    public Long getCompletedImprovements() {
        return completedImprovements;
    }

    public void setCompletedImprovements(Long completedImprovements) {
        this.completedImprovements = completedImprovements;
    }

    public Integer getImprovementCompletionPercentage() {
        return improvementCompletionPercentage;
    }

    public void setImprovementCompletionPercentage(Integer improvementCompletionPercentage) {
        this.improvementCompletionPercentage = improvementCompletionPercentage;
    }

    public Long getTotalActiveExceptions() {
        return totalActiveExceptions;
    }

    public void setTotalActiveExceptions(Long totalActiveExceptions) {
        this.totalActiveExceptions = totalActiveExceptions;
    }

    public Long getTotalExpiredExceptions() {
        return totalExpiredExceptions;
    }

    public void setTotalExpiredExceptions(Long totalExpiredExceptions) {
        this.totalExpiredExceptions = totalExpiredExceptions;
    }

    public LocalDate getNextScheduledAudit() {
        return nextScheduledAudit;
    }

    public void setNextScheduledAudit(LocalDate nextScheduledAudit) {
        this.nextScheduledAudit = nextScheduledAudit;
    }

    public Integer getAverageSiteScore() {
        return averageSiteScore;
    }

    public void setAverageSiteScore(Integer averageSiteScore) {
        this.averageSiteScore = averageSiteScore;
    }

    public Integer getAverageAuditScore() {
        return averageAuditScore;
    }

    public void setAverageAuditScore(Integer averageAuditScore) {
        this.averageAuditScore = averageAuditScore;
    }

    public Long getCriticalFindings() {
        return criticalFindings;
    }

    public void setCriticalFindings(Long criticalFindings) {
        this.criticalFindings = criticalFindings;
    }

    public Long getHighFindings() {
        return highFindings;
    }

    public void setHighFindings(Long highFindings) {
        this.highFindings = highFindings;
    }

    public Long getMediumFindings() {
        return mediumFindings;
    }

    public void setMediumFindings(Long mediumFindings) {
        this.mediumFindings = mediumFindings;
    }

    public Long getLowFindings() {
        return lowFindings;
    }

    public void setLowFindings(Long lowFindings) {
        this.lowFindings = lowFindings;
    }

    public Integer getCompletionPercentage() {
        return completionPercentage;
    }

    public void setCompletionPercentage(Integer completionPercentage) {
        this.completionPercentage = completionPercentage;
    }

    public String getScoreTrendLabel() {
        return scoreTrendLabel;
    }

    public void setScoreTrendLabel(String scoreTrendLabel) {
        this.scoreTrendLabel = scoreTrendLabel;
    }

    public String getScoreTrendClass() {
        return scoreTrendClass;
    }

    public void setScoreTrendClass(String scoreTrendClass) {
        this.scoreTrendClass = scoreTrendClass;
    }

    public String getOpenFindingsTrendLabel() {
        return openFindingsTrendLabel;
    }

    public void setOpenFindingsTrendLabel(String openFindingsTrendLabel) {
        this.openFindingsTrendLabel = openFindingsTrendLabel;
    }

    public String getOpenFindingsTrendClass() {
        return openFindingsTrendClass;
    }

    public void setOpenFindingsTrendClass(String openFindingsTrendClass) {
        this.openFindingsTrendClass = openFindingsTrendClass;
    }

    public String getClosedFindingsTrendLabel() {
        return closedFindingsTrendLabel;
    }

    public void setClosedFindingsTrendLabel(String closedFindingsTrendLabel) {
        this.closedFindingsTrendLabel = closedFindingsTrendLabel;
    }

    public String getClosedFindingsTrendClass() {
        return closedFindingsTrendClass;
    }

    public void setClosedFindingsTrendClass(String closedFindingsTrendClass) {
        this.closedFindingsTrendClass = closedFindingsTrendClass;
    }

    public String getCompletionTrendLabel() {
        return completionTrendLabel;
    }

    public void setCompletionTrendLabel(String completionTrendLabel) {
        this.completionTrendLabel = completionTrendLabel;
    }

    public String getCompletionTrendClass() {
        return completionTrendClass;
    }

    public void setCompletionTrendClass(String completionTrendClass) {
        this.completionTrendClass = completionTrendClass;
    }

    public List<String> getCategoryOptions() {
        return categoryOptions;
    }

    public void setCategoryOptions(List<String> categoryOptions) {
        this.categoryOptions = categoryOptions;
    }

    public List<Integer> getAuditRoundOptions() {
        return auditRoundOptions;
    }

    public void setAuditRoundOptions(List<Integer> auditRoundOptions) {
        this.auditRoundOptions = auditRoundOptions;
    }

    public List<Integer> getYearOptions() {
        return yearOptions;
    }

    public void setYearOptions(List<Integer> yearOptions) {
        this.yearOptions = yearOptions;
    }

    public List<String> getSecurityScoreTrendLabels() {
        return securityScoreTrendLabels;
    }

    public void setSecurityScoreTrendLabels(List<String> securityScoreTrendLabels) {
        this.securityScoreTrendLabels = securityScoreTrendLabels;
    }

    public List<Integer> getSecurityScoreTrendData() {
        return securityScoreTrendData;
    }

    public void setSecurityScoreTrendData(List<Integer> securityScoreTrendData) {
        this.securityScoreTrendData = securityScoreTrendData;
    }

    public List<String> getRiskDistributionLabels() {
        return riskDistributionLabels;
    }

    public void setRiskDistributionLabels(List<String> riskDistributionLabels) {
        this.riskDistributionLabels = riskDistributionLabels;
    }

    public List<Long> getRiskDistributionData() {
        return riskDistributionData;
    }

    public void setRiskDistributionData(List<Long> riskDistributionData) {
        this.riskDistributionData = riskDistributionData;
    }

    public List<String> getFindingsSeverityLabels() {
        return findingsSeverityLabels;
    }

    public void setFindingsSeverityLabels(List<String> findingsSeverityLabels) {
        this.findingsSeverityLabels = findingsSeverityLabels;
    }

    public List<Long> getFindingsSeverityData() {
        return findingsSeverityData;
    }

    public void setFindingsSeverityData(List<Long> findingsSeverityData) {
        this.findingsSeverityData = findingsSeverityData;
    }

    public List<String> getOpenClosedLabels() {
        return openClosedLabels;
    }

    public void setOpenClosedLabels(List<String> openClosedLabels) {
        this.openClosedLabels = openClosedLabels;
    }

    public List<Long> getOpenClosedData() {
        return openClosedData;
    }

    public void setOpenClosedData(List<Long> openClosedData) {
        this.openClosedData = openClosedData;
    }

    public List<String> getAuditProgressLabels() {
        return auditProgressLabels;
    }

    public void setAuditProgressLabels(List<String> auditProgressLabels) {
        this.auditProgressLabels = auditProgressLabels;
    }

    public List<Integer> getAuditProgressData() {
        return auditProgressData;
    }

    public void setAuditProgressData(List<Integer> auditProgressData) {
        this.auditProgressData = auditProgressData;
    }

    public List<String> getAuditAreaLabels() {
        return auditAreaLabels;
    }

    public void setAuditAreaLabels(List<String> auditAreaLabels) {
        this.auditAreaLabels = auditAreaLabels;
    }

    public List<Integer> getAuditAreaData() {
        return auditAreaData;
    }

    public void setAuditAreaData(List<Integer> auditAreaData) {
        this.auditAreaData = auditAreaData;
    }

    public List<RiskItem> getTopRisks() {
        return topRisks;
    }

    public void setTopRisks(List<RiskItem> topRisks) {
        this.topRisks = topRisks;
    }

    public List<AuditActivityItem> getRecentAuditActivity() {
        return recentAuditActivity;
    }

    public void setRecentAuditActivity(List<AuditActivityItem> recentAuditActivity) {
        this.recentAuditActivity = recentAuditActivity;
    }

    public List<ActionPlanItem> getUpcomingActionPlans() {
        return upcomingActionPlans;
    }

    public void setUpcomingActionPlans(List<ActionPlanItem> upcomingActionPlans) {
        this.upcomingActionPlans = upcomingActionPlans;
    }

    public List<ExceptionExpiryItem> getUpcomingExceptionExpiries() {
        return upcomingExceptionExpiries;
    }

    public void setUpcomingExceptionExpiries(List<ExceptionExpiryItem> upcomingExceptionExpiries) {
        this.upcomingExceptionExpiries = upcomingExceptionExpiries;
    }

    public List<SiteHeatmapItem> getSiteHeatmap() {
        return siteHeatmap;
    }

    public void setSiteHeatmap(List<SiteHeatmapItem> siteHeatmap) {
        this.siteHeatmap = siteHeatmap;
    }

    public List<ActivityFeedItem> getActivityFeed() {
        return activityFeed;
    }

    public void setActivityFeed(List<ActivityFeedItem> activityFeed) {
        this.activityFeed = activityFeed;
    }

    public List<SummaryItem> getFindingTypeSummary() {
        return findingTypeSummary;
    }

    public void setFindingTypeSummary(List<SummaryItem> findingTypeSummary) {
        this.findingTypeSummary = findingTypeSummary;
    }

    public List<SummaryItem> getComplianceStatusSummary() {
        return complianceStatusSummary;
    }

    public void setComplianceStatusSummary(List<SummaryItem> complianceStatusSummary) {
        this.complianceStatusSummary = complianceStatusSummary;
    }

    public List<SummaryItem> getVulnerabilityStatusSummary() {
        return vulnerabilityStatusSummary;
    }

    public void setVulnerabilityStatusSummary(List<SummaryItem> vulnerabilityStatusSummary) {
        this.vulnerabilityStatusSummary = vulnerabilityStatusSummary;
    }

    public List<SummaryItem> getVulnerabilitySeveritySummary() {
        return vulnerabilitySeveritySummary;
    }

    public void setVulnerabilitySeveritySummary(List<SummaryItem> vulnerabilitySeveritySummary) {
        this.vulnerabilitySeveritySummary = vulnerabilitySeveritySummary;
    }

    public static class SummaryItem {
        private String label;
        private Long count;

        public SummaryItem() {
        }

        public SummaryItem(String label, Long count) {
            this.label = label;
            this.count = count;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public Long getCount() {
            return count;
        }

        public void setCount(Long count) {
            this.count = count;
        }
    }

    public static class RiskItem {
        private Long findingId;
        private String title;
        private String category;
        private String siteName;
        private String severity;
        private Long count;

        public RiskItem() {
        }

        public Long getFindingId() {
            return findingId;
        }

        public void setFindingId(Long findingId) {
            this.findingId = findingId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getSiteName() {
            return siteName;
        }

        public void setSiteName(String siteName) {
            this.siteName = siteName;
        }

        public String getSeverity() {
            return severity;
        }

        public void setSeverity(String severity) {
            this.severity = severity;
        }

        public Long getCount() {
            return count;
        }

        public void setCount(Long count) {
            this.count = count;
        }
    }

    public static class AuditActivityItem {
        private Long auditId;
        private String siteName;
        private Integer roundNumber;
        private LocalDate auditDate;
        private Integer overallScore;
        private Integer completionPercentage;
        private String auditor;

        public AuditActivityItem() {
        }

        public Long getAuditId() {
            return auditId;
        }

        public void setAuditId(Long auditId) {
            this.auditId = auditId;
        }

        public String getSiteName() {
            return siteName;
        }

        public void setSiteName(String siteName) {
            this.siteName = siteName;
        }

        public Integer getRoundNumber() {
            return roundNumber;
        }

        public void setRoundNumber(Integer roundNumber) {
            this.roundNumber = roundNumber;
        }

        public LocalDate getAuditDate() {
            return auditDate;
        }

        public void setAuditDate(LocalDate auditDate) {
            this.auditDate = auditDate;
        }

        public Integer getOverallScore() {
            return overallScore;
        }

        public void setOverallScore(Integer overallScore) {
            this.overallScore = overallScore;
        }

        public Integer getCompletionPercentage() {
            return completionPercentage;
        }

        public void setCompletionPercentage(Integer completionPercentage) {
            this.completionPercentage = completionPercentage;
        }

        public String getAuditor() {
            return auditor;
        }

        public void setAuditor(String auditor) {
            this.auditor = auditor;
        }
    }

    public static class ActionPlanItem {
        private Long findingId;
        private String action;
        private String description;
        private String owner;
        private String priority;
        private LocalDate targetDate;
        private String status;
        private String notes;

        public ActionPlanItem() {
        }

        public Long getFindingId() {
            return findingId;
        }

        public void setFindingId(Long findingId) {
            this.findingId = findingId;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getOwner() {
            return owner;
        }

        public void setOwner(String owner) {
            this.owner = owner;
        }

        public String getPriority() {
            return priority;
        }

        public void setPriority(String priority) {
            this.priority = priority;
        }

        public LocalDate getTargetDate() {
            return targetDate;
        }

        public void setTargetDate(LocalDate targetDate) {
            this.targetDate = targetDate;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }
    }

    public static class ExceptionExpiryItem {
        private Long exceptionId;
        private String exceptionName;
        private String siteName;
        private String auditLabel;
        private LocalDate expiryDate;
        private String status;

        public ExceptionExpiryItem() {
        }

        public Long getExceptionId() {
            return exceptionId;
        }

        public void setExceptionId(Long exceptionId) {
            this.exceptionId = exceptionId;
        }

        public String getExceptionName() {
            return exceptionName;
        }

        public void setExceptionName(String exceptionName) {
            this.exceptionName = exceptionName;
        }

        public String getSiteName() {
            return siteName;
        }

        public void setSiteName(String siteName) {
            this.siteName = siteName;
        }

        public String getAuditLabel() {
            return auditLabel;
        }

        public void setAuditLabel(String auditLabel) {
            this.auditLabel = auditLabel;
        }

        public LocalDate getExpiryDate() {
            return expiryDate;
        }

        public void setExpiryDate(LocalDate expiryDate) {
            this.expiryDate = expiryDate;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    public static class SiteHeatmapItem {
        private String siteName;
        private Integer latestAuditScore;
        private Long openFindings;
        private Long criticalFindings;
        private Long overdueFindings;
        private String riskLevel;

        public String getSiteName() {
            return siteName;
        }

        public void setSiteName(String siteName) {
            this.siteName = siteName;
        }

        public Integer getLatestAuditScore() {
            return latestAuditScore;
        }

        public void setLatestAuditScore(Integer latestAuditScore) {
            this.latestAuditScore = latestAuditScore;
        }

        public Long getOpenFindings() {
            return openFindings;
        }

        public void setOpenFindings(Long openFindings) {
            this.openFindings = openFindings;
        }

        public Long getCriticalFindings() {
            return criticalFindings;
        }

        public void setCriticalFindings(Long criticalFindings) {
            this.criticalFindings = criticalFindings;
        }

        public Long getOverdueFindings() {
            return overdueFindings;
        }

        public void setOverdueFindings(Long overdueFindings) {
            this.overdueFindings = overdueFindings;
        }

        public String getRiskLevel() {
            return riskLevel;
        }

        public void setRiskLevel(String riskLevel) {
            this.riskLevel = riskLevel;
        }
    }

    public static class ActivityFeedItem {
        private LocalDate activityDate;
        private String type;
        private String message;
        private String badgeClass;
        private String targetUrl;

        public LocalDate getActivityDate() {
            return activityDate;
        }

        public void setActivityDate(LocalDate activityDate) {
            this.activityDate = activityDate;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getBadgeClass() {
            return badgeClass;
        }

        public void setBadgeClass(String badgeClass) {
            this.badgeClass = badgeClass;
        }

        public String getTargetUrl() {
            return targetUrl;
        }

        public void setTargetUrl(String targetUrl) {
            this.targetUrl = targetUrl;
        }
    }
}
