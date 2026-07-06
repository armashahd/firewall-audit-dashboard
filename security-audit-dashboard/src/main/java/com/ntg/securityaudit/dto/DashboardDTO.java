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
    private Long completedImprovements;
    private Integer improvementCompletionPercentage;
    private LocalDate nextScheduledAudit;
    private Integer averageSiteScore;
    private Integer averageAuditScore;
    private Long criticalFindings;
    private Long highFindings;
    private Long mediumFindings;
    private Long lowFindings;
    private Integer completionPercentage;
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

    public static class RiskItem {
        private String title;
        private String category;
        private String severity;
        private Long count;

        public RiskItem() {
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
}
