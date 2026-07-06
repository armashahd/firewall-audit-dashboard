package com.ntg.securityaudit.service;

import com.ntg.securityaudit.entity.AuditException;
import com.ntg.securityaudit.entity.Finding;
import com.ntg.securityaudit.enums.AuditExceptionStatus;
import com.ntg.securityaudit.enums.FindingStatus;
import com.ntg.securityaudit.repository.FindingRepository;
import com.ntg.securityaudit.repository.AuditExceptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class FindingService {

    private final FindingRepository findingRepository;
    private final AuditExceptionRepository auditExceptionRepository;
    private final DatabaseRepairService databaseRepairService;

    public FindingService(FindingRepository findingRepository,
                          AuditExceptionRepository auditExceptionRepository,
                          DatabaseRepairService databaseRepairService) {
        this.findingRepository = findingRepository;
        this.auditExceptionRepository = auditExceptionRepository;
        this.databaseRepairService = databaseRepairService;
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

        normalizeFindingDatesAndStatus(finding);
        Finding savedFinding = findingRepository.save(finding);

        if (savedFinding.getStatus() != null && savedFinding.getStatus().isAcceptedRisk()) {
            ensureAcceptedRiskException(savedFinding);
        } else if (previousStatus != null && previousStatus.isAcceptedRisk()) {
            closeAcceptedRiskException(savedFinding.getId());
        }

        return savedFinding;
    }

    @Transactional
    public void deleteFinding(Long id) {
        if (id != null && findingRepository.existsById(id)) {
            auditExceptionRepository.deleteAll(auditExceptionRepository.findByRelatedFindingId(id));
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
        }

        if (finding.getStatus() == FindingStatus.OPEN || finding.getStatus() == FindingStatus.IN_PROGRESS) {
            // due date remains the remediation deadline; if absent, controller validation rejects it
        }
    }

    private void ensureAcceptedRiskException(Finding finding) {
        AuditException auditException = auditExceptionRepository.findTopByRelatedFindingIdOrderByIdDesc(finding.getId());

        if (auditException == null) {
            auditException = new AuditException();
        }

        auditException.setRelatedFinding(finding);
        auditException.setRelatedAudit(finding.getAudit());
        auditException.setRelatedSite(finding.getAudit() != null ? finding.getAudit().getSite() : null);
        auditException.setExceptionName(finding.getTitle());
        auditException.setDescription(firstNonBlank(finding.getRecommendation(), finding.getDescription()));
        auditException.setJustification(firstNonBlank(finding.getRecommendation(), finding.getDescription()));
        auditException.setStatus(AuditExceptionStatus.ACTIVE);
        auditException.setApprovalDate(LocalDate.now());
        if (auditException.getExpiryDate() == null) {
            auditException.setExpiryDate(LocalDate.now().plusDays(90));
        }
        if (auditException.getApprovedBy() == null) {
            auditException.setApprovedBy("Security Team");
        }
        auditExceptionRepository.save(auditException);
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
