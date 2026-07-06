package com.ntg.securityaudit.entity;

import com.ntg.securityaudit.enums.AuditExceptionStatus;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "audit_exceptions")
public class AuditException {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "related_site_id")
    private Site relatedSite;

    @ManyToOne
    @JoinColumn(name = "audit_id")
    private Audit relatedAudit;

    @ManyToOne
    @JoinColumn(name = "finding_id")
    private Finding relatedFinding;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(length = 3000)
    private String description;

    @Column(name = "reason", length = 3000)
    private String reason;

    private String approvedBy;

    private LocalDate approvedDate;

    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    private AuditExceptionStatus status;

    public AuditException() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Site getRelatedSite() {
        return relatedSite;
    }

    public void setRelatedSite(Site relatedSite) {
        this.relatedSite = relatedSite;
    }

    public Audit getRelatedAudit() {
        return relatedAudit;
    }

    public void setRelatedAudit(Audit relatedAudit) {
        this.relatedAudit = relatedAudit;
    }

    public Finding getRelatedFinding() {
        return relatedFinding;
    }

    public void setRelatedFinding(Finding relatedFinding) {
        this.relatedFinding = relatedFinding;
    }

    public Audit getAudit() {
        return relatedAudit;
    }

    public void setAudit(Audit audit) {
        this.relatedAudit = audit;
    }

    public String getExceptionName() {
        return title;
    }

    public void setExceptionName(String exceptionName) {
        this.title = exceptionName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getJustification() {
        return reason;
    }

    public void setJustification(String justification) {
        this.reason = justification;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public LocalDate getApprovalDate() {
        return approvedDate;
    }

    public void setApprovalDate(LocalDate approvalDate) {
        this.approvedDate = approvalDate;
    }

    public LocalDate getApprovedDate() {
        return approvedDate;
    }

    public void setApprovedDate(LocalDate approvedDate) {
        this.approvedDate = approvedDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public AuditExceptionStatus getStatus() {
        return status;
    }

    public void setStatus(AuditExceptionStatus status) {
        this.status = status;
    }
}
