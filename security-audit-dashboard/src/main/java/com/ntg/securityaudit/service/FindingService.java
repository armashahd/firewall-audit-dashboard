package com.ntg.securityaudit.service;

import com.ntg.securityaudit.entity.AuditException;
import com.ntg.securityaudit.entity.Finding;
import com.ntg.securityaudit.entity.FindingActivityLog;
import com.ntg.securityaudit.enums.FindingActionType;
import com.ntg.securityaudit.enums.AuditExceptionStatus;
import com.ntg.securityaudit.enums.FindingStatus;
import com.ntg.securityaudit.repository.FindingRepository;
import com.ntg.securityaudit.repository.AuditExceptionRepository;
import com.ntg.securityaudit.repository.FindingActivityLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class FindingService {

    private final FindingRepository findingRepository;
    private final AuditExceptionRepository auditExceptionRepository;
    private final FindingActivityLogRepository findingActivityLogRepository;
    private final DatabaseRepairService databaseRepairService;
    private final ActivityLogService activityLogService;

    public FindingService(FindingRepository findingRepository,
                          AuditExceptionRepository auditExceptionRepository,
                          FindingActivityLogRepository findingActivityLogRepository,
                          DatabaseRepairService databaseRepairService,
                          ActivityLogService activityLogService) {
        this.findingRepository = findingRepository;
        this.auditExceptionRepository = auditExceptionRepository;
        this.findingActivityLogRepository = findingActivityLogRepository;
        this.databaseRepairService = databaseRepairService;
        this.activityLogService = activityLogService;
    }

    public List<Finding> getAllFindings() {
        databaseRepairService.repairIfNeeded();
        return findingRepository.findAll();
    }

    public Finding getFindingById(Long id) {
        databaseRepairService.repairIfNeeded();
        return findingRepository.findById(id).orElse(null);
    }

    @Transactional
    public Finding saveFinding(Finding finding) {
        databaseRepairService.repairIfNeeded();

        Finding existingFinding = finding.getId() != null
                ? findingRepository.findById(finding.getId()).orElse(null)
                : null;
        FindingStatus previousStatus = existingFinding != null ? existingFinding.getStatus() : null;
        LocalDate previousDueDate = existingFinding != null ? existingFinding.getDueDate() : null;

        normalizeFindingDatesAndStatus(finding);
        Finding savedFinding = findingRepository.save(finding);

        if (!Objects.equals(previousDueDate, savedFinding.getDueDate())) {
            activityLogService.log(
                    previousDueDate == null ? "DUE_DATE_ASSIGNED" : "DUE_DATE_CHANGED",
                    "Finding",
                    savedFinding.getId(),
                    savedFinding.getTitle(),
                    previousDueDate != null ? previousDueDate.toString() : null,
                    savedFinding.getDueDate() != null ? savedFinding.getDueDate().toString() : null,
                    null
            );
        }

        if (savedFinding.getStatus() != null && savedFinding.getStatus().isAcceptedRisk()) {
            ensureAcceptedRiskException(savedFinding);
        } else if (previousStatus != null && previousStatus.isAcceptedRisk()) {
            closeAcceptedRiskException(savedFinding.getId());
        }

        return savedFinding;
    }

    @Transactional
    public Finding updateStatus(Long findingId, FindingStatus newStatus, String comment, String username) {
        databaseRepairService.repairIfNeeded();

        Finding finding = findingRepository.findById(findingId).orElseThrow();
        FindingStatus oldStatus = finding.getStatus();
        String safeComment = comment != null ? comment.trim() : "";

        if (newStatus == null) {
            throw new IllegalArgumentException("Status is required.");
        }
        if (newStatus == FindingStatus.CLOSED && !StringUtils.hasText(safeComment)) {
            throw new IllegalArgumentException("Resolution comment is required to close a finding.");
        }
        if (newStatus == FindingStatus.ACCEPTED_RISK && !StringUtils.hasText(safeComment)) {
            throw new IllegalArgumentException("Risk acceptance justification is required.");
        }

        finding.setStatus(newStatus);
        finding.setLastUpdatedBy(firstNonBlank(username, "Security Team"));
        finding.setLastUpdatedDate(LocalDateTime.now());
        if (newStatus == FindingStatus.CLOSED) {
            if (finding.getClosedDate() == null) {
                finding.setClosedDate(LocalDate.now());
            }
            finding.setClosedBy(firstNonBlank(username, "Security Team"));
        } else {
            finding.setClosedDate(null);
            finding.setClosedBy(null);
        }

        Finding savedFinding = findingRepository.save(finding);

        if (newStatus == FindingStatus.ACCEPTED_RISK) {
            ensureAcceptedRiskException(savedFinding, safeComment, username);
        } else if (oldStatus != null && oldStatus.isAcceptedRisk()) {
            closeAcceptedRiskException(savedFinding.getId());
        }

        FindingActivityLog log = new FindingActivityLog();
        log.setFinding(savedFinding);
        log.setUsername(firstNonBlank(username, "Security Team"));
        log.setActionType(resolveActionType(newStatus));
        log.setOldStatus(oldStatus);
        log.setNewStatus(newStatus);
        log.setComment(StringUtils.hasText(safeComment) ? safeComment : null);
        log.setCreatedAt(finding.getLastUpdatedDate());
        findingActivityLogRepository.save(log);

        activityLogService.log(
                "FINDING_STATUS_CHANGED",
                "Finding",
                savedFinding.getId(),
                savedFinding.getTitle(),
                oldStatus != null ? oldStatus.getDisplayName() : null,
                newStatus.getDisplayName(),
                StringUtils.hasText(safeComment) ? safeComment : null
        );
        if (StringUtils.hasText(safeComment)) {
            activityLogService.log(
                    "COMMENT_ADDED",
                    "Finding",
                    savedFinding.getId(),
                    savedFinding.getTitle(),
                    null,
                    null,
                    safeComment
            );
        }
        if (newStatus == FindingStatus.CLOSED) {
            activityLogService.log(
                    "FINDING_CLOSED",
                    "Finding",
                    savedFinding.getId(),
                    savedFinding.getTitle(),
                    oldStatus != null ? oldStatus.getDisplayName() : null,
                    newStatus.getDisplayName(),
                    safeComment
            );
        }
        if (newStatus == FindingStatus.ACCEPTED_RISK) {
            activityLogService.log(
                    "ACCEPTED_RISK_SELECTED",
                    "Finding",
                    savedFinding.getId(),
                    savedFinding.getTitle(),
                    oldStatus != null ? oldStatus.getDisplayName() : null,
                    newStatus.getDisplayName(),
                    safeComment
            );
        }

        return savedFinding;
    }

    @Transactional
    public void deleteFinding(Long id) {
        if (id != null && findingRepository.existsById(id)) {
            auditExceptionRepository.deleteAll(auditExceptionRepository.findByRelatedFindingId(id));
            findingActivityLogRepository.deleteByFindingId(id);
            findingRepository.deleteById(id);
        }
    }

    private void normalizeFindingDatesAndStatus(Finding finding) {
        if (finding.getClosedDate() != null) {
            finding.setStatus(FindingStatus.CLOSED);
        }

        if (finding.getStatus() == FindingStatus.CLOSED) {
            if (finding.getClosedDate() == null) {
                finding.setClosedDate(LocalDate.now());
            }
        } else {
            finding.setClosedDate(null);
            finding.setClosedBy(null);
        }

        if (finding.getStatus() == FindingStatus.OPEN || finding.getStatus() == FindingStatus.IN_PROGRESS) {
            // due date remains the remediation deadline; if absent, controller validation rejects it
        }
    }

    private void ensureAcceptedRiskException(Finding finding) {
        ensureAcceptedRiskException(finding, null, null);
    }

    private void ensureAcceptedRiskException(Finding finding, String justification, String username) {
        AuditException auditException = auditExceptionRepository.findTopByRelatedFindingIdOrderByIdDesc(finding.getId());

        if (auditException == null) {
            auditException = new AuditException();
        }

        auditException.setRelatedFinding(finding);
        auditException.setRelatedAudit(finding.getAudit());
        auditException.setRelatedSite(finding.getAudit() != null ? finding.getAudit().getSite() : null);
        auditException.setExceptionName(finding.getTitle());
        auditException.setDescription(firstNonBlank(finding.getRecommendation(), finding.getDescription()));
        auditException.setJustification(firstNonBlank(justification, firstNonBlank(finding.getRecommendation(), finding.getDescription())));
        auditException.setStatus(AuditExceptionStatus.ACTIVE);
        auditException.setApprovalDate(LocalDate.now());
        if (auditException.getExpiryDate() == null) {
            auditException.setExpiryDate(LocalDate.now().plusDays(90));
        }
        if (auditException.getApprovedBy() == null) {
            auditException.setApprovedBy(firstNonBlank(username, "Security Team"));
        }
        auditExceptionRepository.save(auditException);
    }

    private FindingActionType resolveActionType(FindingStatus newStatus) {
        if (newStatus == FindingStatus.CLOSED) {
            return FindingActionType.CLOSED;
        }
        if (newStatus == FindingStatus.ACCEPTED_RISK) {
            return FindingActionType.ACCEPTED_RISK;
        }
        return FindingActionType.STATUS_CHANGED;
    }

    private void closeAcceptedRiskException(Long findingId) {
        if (findingId == null) {
            return;
        }

        AuditException auditException = auditExceptionRepository.findTopByRelatedFindingIdOrderByIdDesc(findingId);
        if (auditException != null && auditException.getStatus() != AuditExceptionStatus.CLOSED) {
            auditException.setStatus(AuditExceptionStatus.CLOSED);
            auditExceptionRepository.save(auditException);
        }
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        if (second != null && !second.isBlank()) {
            return second;
        }
        return null;
    }
}
