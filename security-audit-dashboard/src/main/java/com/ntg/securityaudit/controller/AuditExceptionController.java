package com.ntg.securityaudit.controller;

import com.ntg.securityaudit.entity.AuditException;
import com.ntg.securityaudit.enums.AuditExceptionStatus;
import com.ntg.securityaudit.service.ActivityLogService;
import com.ntg.securityaudit.service.AuditExceptionService;
import com.ntg.securityaudit.service.AuditService;
import com.ntg.securityaudit.service.SiteService;
import org.springframework.security.access.prepost.PreAuthorize;
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

@Controller
public class AuditExceptionController {

    private final AuditExceptionService auditExceptionService;
    private final SiteService siteService;
    private final AuditService auditService;
    private final ActivityLogService activityLogService;

    public AuditExceptionController(AuditExceptionService auditExceptionService,
                                    SiteService siteService,
                                    AuditService auditService,
                                    ActivityLogService activityLogService) {
        this.auditExceptionService = auditExceptionService;
        this.siteService = siteService;
        this.auditService = auditService;
        this.activityLogService = activityLogService;
    }

    @GetMapping({"/audit-exceptions", "/exceptions"})
    public String listAuditExceptions(@RequestParam(required = false) AuditExceptionStatus status, Model model) {
        List<AuditException> auditExceptions = auditExceptionService.getAllAuditExceptions().stream()
                .filter(auditException -> status == null || auditException.getStatus() == status)
                .toList();
        model.addAttribute("auditExceptions", auditExceptions);
        return "audit-exceptions";
    }

    @GetMapping("/audit-exceptions/new")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public String showAddAuditExceptionForm(Model model) {
        model.addAttribute("auditException", new AuditException());
        model.addAttribute("sites", siteService.getAllSites());
        model.addAttribute("audits", auditService.getAllAudits());
        model.addAttribute("selectedSiteId", null);
        model.addAttribute("selectedAuditId", null);
        model.addAttribute("formTitle", "Add Audit Exception");
        model.addAttribute("submitLabel", "Save Exception");
        return "audit-exception-form";
    }

    @GetMapping("/audit-exceptions/edit/{id}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public String showEditAuditExceptionForm(@PathVariable Long id, Model model) {
        AuditException auditException = auditExceptionService.getAuditExceptionById(id);
        if (auditException == null) {
            return "redirect:/audit-exceptions";
        }

        model.addAttribute("auditException", auditException);
        model.addAttribute("sites", siteService.getAllSites());
        model.addAttribute("audits", auditService.getAllAudits());
        model.addAttribute("selectedSiteId", auditException.getRelatedSite() != null ? auditException.getRelatedSite().getId() : null);
        model.addAttribute("selectedAuditId", auditException.getRelatedAudit() != null ? auditException.getRelatedAudit().getId() : null);
        model.addAttribute("formTitle", "Edit Audit Exception");
        model.addAttribute("submitLabel", "Update Exception");
        return "audit-exception-form";
    }

    @GetMapping("/audit-exceptions/details/{id}")
    public String showAuditExceptionDetails(@PathVariable Long id, Model model) {
        AuditException auditException = auditExceptionService.getAuditExceptionById(id);
        if (auditException == null) {
            return "redirect:/audit-exceptions";
        }

        model.addAttribute("auditException", auditException);
        return "audit-exception-details";
    }

    @PostMapping("/audit-exceptions")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public String saveAuditException(@ModelAttribute AuditException auditException,
                                     @RequestParam(required = false) Long relatedSiteId,
                                     @RequestParam(required = false) Long relatedAuditId,
                                     Model model) {
        if (!isValidAuditException(auditException, relatedSiteId, relatedAuditId, model)) {
            model.addAttribute("sites", siteService.getAllSites());
            model.addAttribute("audits", auditService.getAllAudits());
            model.addAttribute("selectedSiteId", relatedSiteId);
            model.addAttribute("selectedAuditId", relatedAuditId);
            model.addAttribute("formTitle", auditException.getId() == null ? "Add Audit Exception" : "Edit Audit Exception");
            model.addAttribute("submitLabel", auditException.getId() == null ? "Save Exception" : "Update Exception");
            return "audit-exception-form";
        }

        var selectedAudit = relatedAuditId != null ? auditService.getAuditById(relatedAuditId) : null;
        auditException.setRelatedAudit(selectedAudit);
        auditException.setRelatedSite(relatedSiteId != null ? siteService.getSiteById(relatedSiteId) : (selectedAudit != null ? selectedAudit.getSite() : null));
        auditException.setStatus(auditException.getExpiryDate() != null && auditException.getExpiryDate().isBefore(LocalDate.now())
                ? AuditExceptionStatus.EXPIRED
                : AuditExceptionStatus.ACTIVE);
        boolean created = auditException.getId() == null;
        AuditException savedException = auditExceptionService.saveAuditException(auditException);
        if (created) {
            activityLogService.log(
                    "AUDIT_EXCEPTION_CREATED",
                    "Audit Exception",
                    savedException.getId(),
                    savedException.getExceptionName(),
                    null,
                    savedException.getStatus() != null ? savedException.getStatus().name() : null,
                    savedException.getJustification()
            );
        }
        return "redirect:/audit-exceptions";
    }

    @PostMapping("/audit-exceptions/{id}/delete")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public String deleteAuditException(@PathVariable Long id) {
        auditExceptionService.deleteAuditException(id);
        return "redirect:/audit-exceptions";
    }

    private boolean isValidAuditException(AuditException auditException, Long relatedSiteId, Long relatedAuditId, Model model) {
        boolean valid = true;

        if (!StringUtils.hasText(auditException.getExceptionName())) {
            model.addAttribute("exceptionNameError", "Exception name is required.");
            valid = false;
        }

        if (!StringUtils.hasText(auditException.getDescription())) {
            model.addAttribute("descriptionError", "Description is required.");
            valid = false;
        }

        if (!StringUtils.hasText(auditException.getJustification())) {
            model.addAttribute("justificationError", "Justification is required.");
            valid = false;
        }

        if (!StringUtils.hasText(auditException.getApprovedBy())) {
            model.addAttribute("approvedByError", "Approved by is required.");
            valid = false;
        }

        if (auditException.getApprovalDate() == null) {
            model.addAttribute("approvalDateError", "Approval date is required.");
            valid = false;
        }

        if (auditException.getExpiryDate() == null) {
            model.addAttribute("expiryDateError", "Expiry date is required.");
            valid = false;
        }

        if (auditException.getApprovalDate() != null && auditException.getExpiryDate() != null
                && auditException.getExpiryDate().isBefore(auditException.getApprovalDate())) {
            model.addAttribute("expiryDateError", "Expiry date cannot be before approval date.");
            valid = false;
        }

        if (auditException.getStatus() == null) {
            model.addAttribute("statusError", "Status is required.");
            valid = false;
        }

        if (relatedAuditId == null) {
            model.addAttribute("auditError", "Related audit is required.");
            valid = false;
        }

        if (relatedSiteId != null && siteService.getSiteById(relatedSiteId) == null) {
            model.addAttribute("siteError", "Selected site does not exist.");
            valid = false;
        }

        if (relatedAuditId != null && auditService.getAuditById(relatedAuditId) == null) {
            model.addAttribute("auditError", "Selected audit does not exist.");
            valid = false;
        }

        if (relatedSiteId != null && relatedAuditId != null) {
            var selectedAudit = auditService.getAuditById(relatedAuditId);
            if (selectedAudit != null && selectedAudit.getSite() != null && !relatedSiteId.equals(selectedAudit.getSite().getId())) {
                model.addAttribute("siteError", "Selected site must match the selected audit.");
                valid = false;
            }
        }

        if (!valid) {
            model.addAttribute("errorMessage", "Please correct the highlighted fields.");
        }

        return valid;
    }
}
