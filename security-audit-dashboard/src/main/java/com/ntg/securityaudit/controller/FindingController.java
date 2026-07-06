package com.ntg.securityaudit.controller;

import com.ntg.securityaudit.entity.Audit;
import com.ntg.securityaudit.entity.Finding;
import com.ntg.securityaudit.enums.FindingStatus;
import com.ntg.securityaudit.enums.Severity;
import com.ntg.securityaudit.service.AuditService;
import com.ntg.securityaudit.service.FindingService;
import com.ntg.securityaudit.service.SiteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Controller
public class FindingController {

    private final FindingService findingService;
    private final AuditService auditService;
    private final SiteService siteService;

    public FindingController(FindingService findingService, AuditService auditService, SiteService siteService) {
        this.findingService = findingService;
        this.auditService = auditService;
        this.siteService = siteService;
    }

    @GetMapping("/findings")
    public String listFindings(@RequestParam(required = false) Long siteId,
                               @RequestParam(required = false) FindingStatus status,
                               @RequestParam(required = false) Severity severity,
                               @RequestParam(required = false) String category,
                               @RequestParam(required = false) Boolean overdue,
                               Model model) {
        List<Finding> allFindings = findingService.getAllFindings();
        List<Finding> findings = allFindings.stream()
                .filter(finding -> siteId == null || finding.getAudit() != null
                        && finding.getAudit().getSite() != null
                        && siteId.equals(finding.getAudit().getSite().getId()))
                .filter(finding -> status == null || finding.getStatus() == status)
                .filter(finding -> severity == null || finding.getSeverity() == severity)
                .filter(finding -> !StringUtils.hasText(category) || category.equalsIgnoreCase(finding.getCategory()))
                .filter(finding -> overdue == null || !overdue || finding.getStatus() != null
                        && finding.getStatus().isOpenOrInProgress()
                        && finding.getDueDate() != null
                        && finding.getDueDate().isBefore(LocalDate.now()))
                .toList();
        List<String> categories = allFindings.stream()
                .map(Finding::getCategory)
                .filter(StringUtils::hasText)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();

        model.addAttribute("findings", findings);
        model.addAttribute("sites", siteService.getAllSites());
        model.addAttribute("severities", Severity.values());
        model.addAttribute("statuses", FindingStatus.values());
        model.addAttribute("categories", categories);
        model.addAttribute("selectedSiteId", siteId);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedSeverity", severity);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedOverdue", Objects.equals(overdue, Boolean.TRUE));
        return "findings";
    }

    @GetMapping("/findings/new")
    public String showAddFindingForm(Model model) {
        model.addAttribute("finding", new Finding());
        model.addAttribute("audits", auditService.getAllAudits());
        model.addAttribute("selectedAuditId", null);
        model.addAttribute("formTitle", "Add New Finding");
        model.addAttribute("submitLabel", "Save Finding");
        return "finding-form";
    }

    @GetMapping("/findings/edit/{id}")
    public String showEditFindingForm(@PathVariable Long id, Model model) {
        Finding finding = findingService.getFindingById(id);
        if (finding == null) {
            return "redirect:/findings";
        }

        model.addAttribute("finding", finding);
        model.addAttribute("audits", auditService.getAllAudits());
        model.addAttribute("selectedAuditId", finding.getAudit() != null ? finding.getAudit().getId() : null);
        model.addAttribute("formTitle", "Edit Finding");
        model.addAttribute("submitLabel", "Update Finding");
        return "finding-form";
    }

    @GetMapping("/findings/details/{id}")
    public String showFindingDetails(@PathVariable Long id, Model model) {
        Finding finding = findingService.getFindingById(id);
        if (finding == null) {
            return "redirect:/findings";
        }

        model.addAttribute("finding", finding);
        return "finding-details";
    }

    @PostMapping("/findings")
    public String saveFinding(@ModelAttribute Finding finding, @RequestParam(required = false) Long auditId, Model model) {
        if (!isValidFinding(finding, auditId, model)) {
            model.addAttribute("audits", auditService.getAllAudits());
            model.addAttribute("selectedAuditId", auditId);
            model.addAttribute("formTitle", finding.getId() == null ? "Add New Finding" : "Edit Finding");
            model.addAttribute("submitLabel", finding.getId() == null ? "Save Finding" : "Update Finding");
            return "finding-form";
        }

        Audit audit = auditService.getAuditById(auditId);
        finding.setAudit(audit);
        findingService.saveFinding(finding);
        return "redirect:/findings";
    }

    @PostMapping("/findings/{id}/delete")
    public String deleteFinding(@PathVariable Long id) {
        findingService.deleteFinding(id);
        return "redirect:/findings";
    }

    private boolean isValidFinding(Finding finding, Long auditId, Model model) {
        boolean valid = true;

        if (auditId == null) {
            model.addAttribute("auditError", "Audit is required.");
            valid = false;
        } else if (auditService.getAuditById(auditId) == null) {
            model.addAttribute("auditError", "Selected audit does not exist.");
            valid = false;
        }

        if (!StringUtils.hasText(finding.getTitle())) {
            model.addAttribute("titleError", "Title is required.");
            valid = false;
        }

        if (!StringUtils.hasText(finding.getDescription())) {
            model.addAttribute("descriptionError", "Description is required.");
            valid = false;
        }

        if (finding.getSeverity() == null) {
            model.addAttribute("severityError", "Severity is required.");
            valid = false;
        }

        if (finding.getStatus() == null) {
            model.addAttribute("statusError", "Status is required.");
            valid = false;
        }

        boolean closedByDate = finding.getClosedDate() != null;
        boolean effectiveClosed = finding.getStatus() == FindingStatus.CLOSED || closedByDate;

        if (finding.getDueDate() != null && finding.getClosedDate() != null && finding.getDueDate().isAfter(finding.getClosedDate())) {
            model.addAttribute("dueDateError", "Due date cannot be after closed date.");
            valid = false;
        }

        if (finding.getStatus() != null && !effectiveClosed && finding.getStatus() != FindingStatus.ACCEPTED_RISK
                && finding.getDueDate() == null) {
            model.addAttribute("dueDateError", "Due date is required for open and in-progress findings.");
            valid = false;
        }

        if (finding.getStatus() == FindingStatus.CLOSED && finding.getClosedDate() == null) {
            finding.setClosedDate(java.time.LocalDate.now());
        }

        if (!StringUtils.hasText(finding.getCategory())) {
            model.addAttribute("categoryError", "Category is required.");
            valid = false;
        }

        if (finding.getStatus() != FindingStatus.ACCEPTED_RISK && !StringUtils.hasText(finding.getRecommendation())) {
            model.addAttribute("recommendationError", "Recommendation is required.");
            valid = false;
        }

        if (!valid) {
            model.addAttribute("errorMessage", "Please correct the highlighted fields.");
        }

        return valid;
    }
}
