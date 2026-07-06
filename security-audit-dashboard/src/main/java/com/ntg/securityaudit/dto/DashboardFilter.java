package com.ntg.securityaudit.dto;

import com.ntg.securityaudit.enums.FindingStatus;
import com.ntg.securityaudit.enums.Severity;

public class DashboardFilter {

    private Long siteId;
    private Integer auditRound;
    private String category;
    private Severity severity;
    private FindingStatus status;
    private Integer year;

    public Long getSiteId() {
        return siteId;
    }

    public void setSiteId(Long siteId) {
        this.siteId = siteId;
    }

    public Integer getAuditRound() {
        return auditRound;
    }

    public void setAuditRound(Integer auditRound) {
        this.auditRound = auditRound;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public FindingStatus getStatus() {
        return status;
    }

    public void setStatus(FindingStatus status) {
        this.status = status;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }
}
