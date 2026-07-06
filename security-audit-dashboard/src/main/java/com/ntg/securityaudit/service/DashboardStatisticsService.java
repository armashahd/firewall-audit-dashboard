package com.ntg.securityaudit.service;

import com.ntg.securityaudit.entity.AuditException;
import com.ntg.securityaudit.entity.Finding;
import com.ntg.securityaudit.enums.AuditExceptionStatus;
import com.ntg.securityaudit.enums.Severity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class DashboardStatisticsService {

    public boolean isOpenOrInProgress(Finding finding) {
        return finding.getStatus() != null && finding.getStatus().isOpenOrInProgress();
    }

    public boolean isOverdue(Finding finding) {
        return isOpenOrInProgress(finding) && finding.getDueDate() != null && finding.getDueDate().isBefore(LocalDate.now());
    }

    public AuditExceptionStatus effectiveExceptionStatus(AuditException exception) {
        if (exception.getStatus() == AuditExceptionStatus.CLOSED) {
            return AuditExceptionStatus.CLOSED;
        }
        if (exception.getExpiryDate() != null && exception.getExpiryDate().isBefore(LocalDate.now())) {
            return AuditExceptionStatus.EXPIRED;
        }
        return AuditExceptionStatus.ACTIVE;
    }

    public String resolveRiskLevel(List<Finding> findings, int averageSecurityScore) {
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
}
