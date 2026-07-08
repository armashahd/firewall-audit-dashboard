package com.ntg.securityaudit.controller;

import com.ntg.securityaudit.dto.ReportUploadResult;
import com.ntg.securityaudit.entity.Audit;
import com.ntg.securityaudit.entity.Report;
import com.ntg.securityaudit.service.AuditService;
import com.ntg.securityaudit.service.ActivityLogService;
import com.ntg.securityaudit.service.ReportUploadService;
import com.ntg.securityaudit.service.ReportService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;

@Controller
public class ReportController {

    private final ReportService reportService;
    private final AuditService auditService;
    private final ReportUploadService reportUploadService;
    private final ActivityLogService activityLogService;

    public ReportController(ReportService reportService,
                            AuditService auditService,
                            ReportUploadService reportUploadService,
                            ActivityLogService activityLogService) {
        this.reportService = reportService;
        this.auditService = auditService;
        this.reportUploadService = reportUploadService;
        this.activityLogService = activityLogService;
    }

    @GetMapping("/reports")
    public String listReports(Model model) {
        model.addAttribute("reports", reportService.getAllReports());
        return "reports";
    }

    @GetMapping("/reports/new")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public String showAddReportForm(Model model) {
        model.addAttribute("report", new Report());
        model.addAttribute("audits", auditService.getAllAudits());
        model.addAttribute("selectedAuditId", null);
        model.addAttribute("formTitle", "Add New Report");
        model.addAttribute("submitLabel", "Save Report");
        return "report-form";
    }

    @GetMapping("/reports/upload")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public String showUploadReportForm(Model model) {
        model.addAttribute("uploadError", null);
        return "report-upload";
    }

    @PostMapping("/reports/upload")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public String uploadReport(@RequestParam("file") MultipartFile file, Model model) {
        try {
            ReportUploadResult result = reportUploadService.uploadAuditReport(file);
            model.addAttribute("result", result);
            if (!result.isDuplicate()) {
                activityLogService.log(
                        "PDF_REPORT_UPLOADED",
                        "Report",
                        result.getReportId(),
                        file.getOriginalFilename(),
                        null,
                        "Imported " + result.getFindingCount() + " finding(s)",
                        result.getMessage()
                );
            }
            return "report-upload-result";
        } catch (IllegalArgumentException | IOException ex) {
            model.addAttribute("uploadError", ex.getMessage());
            return "report-upload";
        }
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleUploadSizeExceeded(Model model) {
        model.addAttribute("uploadError", "The selected PDF is too large. Please upload a file up to 100 MB.");
        return "report-upload";
    }

    @GetMapping("/reports/edit/{id}")
    @PreAuthorize("hasRole('SUPERADMIN')")
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
    @PreAuthorize("hasRole('SUPERADMIN')")
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
    @PreAuthorize("hasRole('SUPERADMIN')")
    public String deleteReport(@PathVariable Long id) {
        reportService.deleteReport(id);
        return "redirect:/reports";
    }

    @GetMapping("/reports/{id}/download")
    public ResponseEntity<Resource> downloadOriginalReport(@PathVariable Long id) {
        Report report = reportService.getReportById(id);
        if (report == null || !StringUtils.hasText(report.getFilePath())) {
            return ResponseEntity.notFound().build();
        }

        FileSystemResource resource = new FileSystemResource(Path.of(report.getFilePath()));
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + report.getFileName() + "\"")
                .body(resource);
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
