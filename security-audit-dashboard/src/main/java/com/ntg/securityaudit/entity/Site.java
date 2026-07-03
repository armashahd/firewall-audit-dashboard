package com.ntg.securityaudit.entity;

import com.ntg.securityaudit.enums.SiteStatus;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "sites")
public class Site {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String country;

    private String city;

    private String firewallVendor;

    private String firewallModel;

    private String firmwareVersion;

    private LocalDate lastAuditDate;

    private Integer securityScore;

    @Enumerated(EnumType.STRING)
    private SiteStatus status;

    public Site() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getFirewallVendor() {
        return firewallVendor;
    }

    public void setFirewallVendor(String firewallVendor) {
        this.firewallVendor = firewallVendor;
    }

    public String getFirewallModel() {
        return firewallModel;
    }

    public void setFirewallModel(String firewallModel) {
        this.firewallModel = firewallModel;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public LocalDate getLastAuditDate() {
        return lastAuditDate;
    }

    public void setLastAuditDate(LocalDate lastAuditDate) {
        this.lastAuditDate = lastAuditDate;
    }

    public Integer getSecurityScore() {
        return securityScore;
    }

    public void setSecurityScore(Integer securityScore) {
        this.securityScore = securityScore;
    }

    public SiteStatus getStatus() {
        return status;
    }

    public void setStatus(SiteStatus status) {
        this.status = status;
    }
}