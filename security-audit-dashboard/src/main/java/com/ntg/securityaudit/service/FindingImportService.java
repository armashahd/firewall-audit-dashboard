package com.ntg.securityaudit.service;

import com.ntg.securityaudit.dto.ParsedFinding;
import com.ntg.securityaudit.entity.Audit;
import com.ntg.securityaudit.entity.Finding;
import com.ntg.securityaudit.enums.FindingStatus;
import com.ntg.securityaudit.repository.FindingRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class FindingImportService {

    private final FindingRepository findingRepository;

    public FindingImportService(FindingRepository findingRepository) {
        this.findingRepository = findingRepository;
    }

    public int importFindings(Audit audit, List<ParsedFinding> parsedFindings) {
        if (audit == null || parsedFindings == null || parsedFindings.isEmpty()) {
            return 0;
        }

        LocalDate dueDate = audit.getAuditDate() != null ? audit.getAuditDate().plusDays(30) : LocalDate.now().plusDays(30);
        List<Finding> findings = parsedFindings.stream()
                .map(parsedFinding -> toFinding(audit, parsedFinding, dueDate))
                .toList();
        findingRepository.saveAll(findings);
        return findings.size();
    }

    private Finding toFinding(Audit audit, ParsedFinding parsedFinding, LocalDate dueDate) {
        Finding finding = new Finding();
        finding.setAudit(audit);
        finding.setTitle(parsedFinding.getTitle());
        finding.setSeverity(parsedFinding.getSeverity());
        finding.setCategory(parsedFinding.getCategory());
        finding.setDescription(parsedFinding.getDescription());
        finding.setImpact(parsedFinding.getImpact());
        finding.setRecommendation(parsedFinding.getRecommendation());
        finding.setStatus(FindingStatus.OPEN);
        finding.setAssignedTo("Compliance".equalsIgnoreCase(parsedFinding.getCategory()) ? "Security Team" : "Network Team");
        finding.setDueDate(dueDate);
        return finding;
    }
}
