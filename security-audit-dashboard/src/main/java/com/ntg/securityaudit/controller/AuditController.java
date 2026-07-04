package com.ntg.securityaudit.controller;

import com.ntg.securityaudit.entity.Audit;
import com.ntg.securityaudit.entity.Site;
import com.ntg.securityaudit.service.AuditService;
import com.ntg.securityaudit.service.SiteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuditController {

    private final AuditService auditService;
    private final SiteService siteService;

    public AuditController(AuditService auditService, SiteService siteService) {
        this.auditService = auditService;
        this.siteService = siteService;
    }

    @GetMapping("/audits")
    public String listAudits(Model model) {
        model.addAttribute("audits", auditService.getAllAudits());
        return "audits";
    }

    @GetMapping("/audits/new")
    public String showAddAuditForm(Model model) {
        Audit audit = new Audit();
        model.addAttribute("audit", audit);
        model.addAttribute("sites", siteService.getAllSites());
        model.addAttribute("selectedSiteId", null);
        model.addAttribute("formTitle", "Add New Audit");
        model.addAttribute("submitLabel", "Save Audit");
        return "audit-form";
    }

    @GetMapping("/audits/edit/{id}")
    public String showEditAuditForm(@PathVariable Long id, Model model) {
        Audit audit = auditService.getAuditById(id);
        if (audit == null) {
            return "redirect:/audits";
        }

        model.addAttribute("audit", audit);
        model.addAttribute("sites", siteService.getAllSites());
        model.addAttribute("selectedSiteId", audit.getSite() != null ? audit.getSite().getId() : null);
        model.addAttribute("formTitle", "Edit Audit");
        model.addAttribute("submitLabel", "Update Audit");
        return "audit-form";
    }

    @GetMapping("/audits/details/{id}")
    public String showAuditDetails(@PathVariable Long id, Model model) {
        Audit audit = auditService.getAuditById(id);
        if (audit == null) {
            return "redirect:/audits";
        }

        model.addAttribute("audit", audit);
        return "audit-details";
    }

    @PostMapping("/audits")
    public String saveAudit(@ModelAttribute Audit audit, @RequestParam(required = false) Long siteId, Model model) {
        if (!isValidAudit(audit, siteId, model)) {
            model.addAttribute("sites", siteService.getAllSites());
            model.addAttribute("selectedSiteId", siteId);
            model.addAttribute("formTitle", audit.getId() == null ? "Add New Audit" : "Edit Audit");
            model.addAttribute("submitLabel", audit.getId() == null ? "Save Audit" : "Update Audit");
            return "audit-form";
        }

        Site site = siteService.getSiteById(siteId);
        audit.setSite(site);
        auditService.saveAudit(audit);
        return "redirect:/audits";
    }

    @PostMapping("/audits/{id}/delete")
    public String deleteAudit(@PathVariable Long id) {
        auditService.deleteAudit(id);
        return "redirect:/audits";
    }

    private boolean isValidAudit(Audit audit, Long siteId, Model model) {
        boolean valid = true;

        if (siteId == null) {
            model.addAttribute("siteError", "Site is required.");
            valid = false;
        } else if (siteService.getSiteById(siteId) == null) {
            model.addAttribute("siteError", "Selected site does not exist.");
            valid = false;
        }

        if (audit.getAuditRound() == null || audit.getAuditRound() <= 0) {
            model.addAttribute("auditRoundError", "Audit round must be greater than 0.");
            valid = false;
        }

        if (audit.getAuditDate() == null) {
            model.addAttribute("auditDateError", "Audit date is required.");
            valid = false;
        }

        if (!StringUtils.hasText(audit.getAuditor())) {
            model.addAttribute("auditorError", "Auditor is required.");
            valid = false;
        }

        if (audit.getOverallScore() == null) {
            model.addAttribute("overallScoreError", "Overall score is required.");
            valid = false;
        } else if (audit.getOverallScore() < 0 || audit.getOverallScore() > 100) {
            model.addAttribute("overallScoreError", "Overall score must be between 0 and 100.");
            valid = false;
        }

        if (audit.getCompletionPercentage() == null) {
            model.addAttribute("completionPercentageError", "Completion percentage is required.");
            valid = false;
        } else if (audit.getCompletionPercentage() < 0 || audit.getCompletionPercentage() > 100) {
            model.addAttribute("completionPercentageError", "Completion percentage must be between 0 and 100.");
            valid = false;
        }

        if (!StringUtils.hasText(audit.getRemarks())) {
            model.addAttribute("remarksError", "Remarks are required.");
            valid = false;
        }

        if (!valid) {
            model.addAttribute("errorMessage", "Please correct the highlighted fields.");
        }

        return valid;
    }
}
