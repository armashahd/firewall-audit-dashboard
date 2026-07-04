package com.ntg.securityaudit.controller;

import com.ntg.securityaudit.entity.Audit;
import com.ntg.securityaudit.entity.Report;
import com.ntg.securityaudit.service.AuditService;
import com.ntg.securityaudit.service.ReportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
public class ReportController {

    private final ReportService reportService;
    private final AuditService auditService;

    public ReportController(ReportService reportService, AuditService auditService) {
        this.reportService = reportService;
        this.auditService = auditService;
    }

    @GetMapping("/reports")
    public String listReports(Model model) {
        model.addAttribute("reports", reportService.getAllReports());
        return "reports";
    }

    @GetMapping("/reports/new")
    public String showAddReportForm(Model model) {
        model.addAttribute("report", new Report());
        model.addAttribute("audits", auditService.getAllAudits());
        model.addAttribute("selectedAuditId", null);
        model.addAttribute("formTitle", "Add New Report");
        model.addAttribute("submitLabel", "Save Report");
        return "report-form";
    }

    @GetMapping("/reports/edit/{id}")
    public String showEditReportForm(@PathVariable Long id, Model model) {
        Report report = reportService.getReportById(id);
        if (report == null) {
            return "redirect:/reports";
        }

        model.addAttribute("report", report);
        model.addAttribute("audits", auditService.getAllAudits());
        model.addAttribute("selectedAuditId", report.getAudit() != null ? report.getAudit().getId() : null);
        model.addAttribute("formTitle", "Edit Report");
        model.addAttribute("submitLabel", "Update Report");
        return "report-form";
    }

    @GetMapping("/reports/details/{id}")
    public String showReportDetails(@PathVariable Long id, Model model) {
        Report report = reportService.getReportById(id);
        if (report == null) {
            return "redirect:/reports";
        }

        model.addAttribute("report", report);
        return "report-details";
    }

    @PostMapping("/reports")
    public String saveReport(@ModelAttribute Report report, @RequestParam(required = false) Long auditId, Model model) {
        if (!isValidReport(report, auditId, model)) {
            model.addAttribute("audits", auditService.getAllAudits());
            model.addAttribute("selectedAuditId", auditId);
            model.addAttribute("formTitle", report.getId() == null ? "Add New Report" : "Edit Report");
            model.addAttribute("submitLabel", report.getId() == null ? "Save Report" : "Update Report");
            return "report-form";
        }

        Audit audit = auditService.getAuditById(auditId);
        report.setAudit(audit);
        if (report.getUploadDate() == null) {
            report.setUploadDate(LocalDate.now());
        }
        reportService.saveReport(report);
        return "redirect:/reports";
    }

    @PostMapping("/reports/{id}/delete")
    public String deleteReport(@PathVariable Long id) {
        reportService.deleteReport(id);
        return "redirect:/reports";
    }

    private boolean isValidReport(Report report, Long auditId, Model model) {
        boolean valid = true;

        if (auditId == null) {
            model.addAttribute("auditError", "Audit is required.");
            valid = false;
        } else if (auditService.getAuditById(auditId) == null) {
            model.addAttribute("auditError", "Selected audit does not exist.");
            valid = false;
        }

        if (!StringUtils.hasText(report.getFileName())) {
            model.addAttribute("fileNameError", "File name is required.");
            valid = false;
        }

        if (!StringUtils.hasText(report.getFilePath())) {
            model.addAttribute("filePathError", "File path is required.");
            valid = false;
        }

        if (report.getUploadDate() == null) {
            model.addAttribute("uploadDateError", "Upload date is required.");
            valid = false;
        }

        if (!StringUtils.hasText(report.getUploadedBy())) {
            model.addAttribute("uploadedByError", "Uploaded by is required.");
            valid = false;
        }

        if (!valid) {
            model.addAttribute("errorMessage", "Please correct the highlighted fields.");
        }

        return valid;
    }
}
