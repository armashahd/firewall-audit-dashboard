package com.ntg.securityaudit.dto;

public class DashboardDTO {

    private Integer overallSecurityScore;

    private Long totalSites;

    private Long openFindings;

    private Long closedFindings;

    private Long criticalFindings;

    private Long highFindings;

    private Long mediumFindings;

    private Long lowFindings;

    private Integer completionPercentage;

    public DashboardDTO() {
    }

    public Integer getOverallSecurityScore() {
        return overallSecurityScore;
    }

    public void setOverallSecurityScore(Integer overallSecurityScore) {
        this.overallSecurityScore = overallSecurityScore;
    }

    public Long getTotalSites() {
        return totalSites;
    }

    public void setTotalSites(Long totalSites) {
        this.totalSites = totalSites;
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
}