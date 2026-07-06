package com.ntg.securityaudit.dto;

public class ReportUploadResult {

    private Long reportId;
    private String siteName;
    private Integer auditRound;
    private int findingCount;
    private boolean duplicate;
    private String message;

    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public Integer getAuditRound() {
        return auditRound;
    }

    public void setAuditRound(Integer auditRound) {
        this.auditRound = auditRound;
    }

    public int getFindingCount() {
        return findingCount;
    }

    public void setFindingCount(int findingCount) {
        this.findingCount = findingCount;
    }

    public boolean isDuplicate() {
        return duplicate;
    }

    public void setDuplicate(boolean duplicate) {
        this.duplicate = duplicate;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
