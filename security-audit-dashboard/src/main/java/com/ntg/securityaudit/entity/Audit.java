package com.ntg.securityaudit.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "audits")
public class Audit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    private Integer auditRound;

    private LocalDate auditDate;

    private String auditor;

    private Integer overallScore;

    private Integer completionPercentage;

    @Column(length = 1000)
    private String remarks;

    public Audit() {
    }

    public Long getId() {
        return id;
    }

    @Transient
    public String getReferenceNumber() {
        return formatReferenceNumber("AUD", id);
    }

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getAuditRound() {
        return auditRound;
    }

    public void setAuditRound(Integer auditRound) {
        this.auditRound = auditRound;
    }

    public LocalDate getAuditDate() {
        return auditDate;
    }

    public void setAuditDate(LocalDate auditDate) {
        this.auditDate = auditDate;
    }

    public String getAuditor() {
        return auditor;
    }

    public void setAuditor(String auditor) {
        this.auditor = auditor;
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

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    private String formatReferenceNumber(String prefix, Long value) {
        return value == null ? prefix + "-NEW" : "%s-%04d".formatted(prefix, value);
    }
}
