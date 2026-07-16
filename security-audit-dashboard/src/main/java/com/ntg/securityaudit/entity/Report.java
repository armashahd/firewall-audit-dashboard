package com.ntg.securityaudit.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "reports")
public class Report {


    public Long getId() {
        return id;
    }

    @Transient
    public String getReferenceNumber() {
        return formatReferenceNumber("RPT", id);
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Audit getAudit() {
        return audit;
    }

    public void setAudit(Audit audit) {
        this.audit = audit;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public LocalDate getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDate uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
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

    public LocalDate getAssessmentDate() {
        return assessmentDate;
    }

    public void setAssessmentDate(LocalDate assessmentDate) {
        this.assessmentDate = assessmentDate;
    }

    public Integer getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Integer riskScore) {
        this.riskScore = riskScore;
    }

    public Integer getPassedComplianceCount() {
        return passedComplianceCount;
    }

    public void setPassedComplianceCount(Integer passedComplianceCount) {
        this.passedComplianceCount = passedComplianceCount;
    }

    public Integer getFailedComplianceCount() {
        return failedComplianceCount;
    }

    public void setFailedComplianceCount(Integer failedComplianceCount) {
        this.failedComplianceCount = failedComplianceCount;
    }

    public Integer getCriticalCount() {
        return criticalCount;
    }

    public void setCriticalCount(Integer criticalCount) {
        this.criticalCount = criticalCount;
    }

    public Integer getHighCount() {
        return highCount;
    }

    public void setHighCount(Integer highCount) {
        this.highCount = highCount;
    }

    public Integer getMediumCount() {
        return mediumCount;
    }

    public void setMediumCount(Integer mediumCount) {
        this.mediumCount = mediumCount;
    }

    public Integer getLowCount() {
        return lowCount;
    }

    public void setLowCount(Integer lowCount) {
        this.lowCount = lowCount;
    }

    public Integer getInfoCount() {
        return infoCount;
    }

    public void setInfoCount(Integer infoCount) {
        this.infoCount = infoCount;
    }

    private String formatReferenceNumber(String prefix, Long value) {
        return value == null ? prefix + "-NEW" : "%s-%04d".formatted(prefix, value);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "audit_id", nullable = false)
    private Audit audit;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String filePath;

    private String version;

    private LocalDate uploadDate;

    private String uploadedBy;

    private String siteName;

    private String country;

    private String deviceType;

    private String deviceModel;

    private String hostname;

    private String ipAddress;

    private String vendor;

    private LocalDate auditDate;

    private String auditor;

    private LocalDate assessmentDate;

    private Integer riskScore;

    private Integer passedComplianceCount;

    private Integer failedComplianceCount;

    private Integer criticalCount;

    private Integer highCount;

    private Integer mediumCount;

    private Integer lowCount;

    private Integer infoCount;

}
