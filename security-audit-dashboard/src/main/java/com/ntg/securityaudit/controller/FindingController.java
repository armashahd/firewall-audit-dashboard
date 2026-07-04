package com.ntg.securityaudit.controller;

import com.ntg.securityaudit.entity.Audit;
import com.ntg.securityaudit.entity.Finding;
import com.ntg.securityaudit.enums.FindingStatus;
import com.ntg.securityaudit.enums.Severity;
import com.ntg.securityaudit.service.AuditService;
import com.ntg.securityaudit.service.FindingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class FindingController {

    private final FindingService findingService;
    private final AuditService auditService;

    public FindingController(FindingService findingService, AuditService auditService) {
        this.findingService = findingService;
        this.auditService = auditService;
    }

    @GetMapping("/findings")
    public String listFindings(Model model) {
        model.addAttribute("findings", findingService.getAllFindings());
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

        if (!StringUtils.hasText(finding.getCategory())) {
            model.addAttribute("categoryError", "Category is required.");
            valid = false;
        }

        if (!StringUtils.hasText(finding.getRecommendation())) {
            model.addAttribute("recommendationError", "Recommendation is required.");
            valid = false;
        }

        if (!valid) {
            model.addAttribute("errorMessage", "Please correct the highlighted fields.");
        }

        return valid;
    }
}
