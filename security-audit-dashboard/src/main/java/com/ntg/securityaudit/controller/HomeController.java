package com.ntg.securityaudit.controller;

import com.ntg.securityaudit.dto.DashboardDTO;
import com.ntg.securityaudit.dto.DashboardFilter;
import com.ntg.securityaudit.enums.FindingStatus;
import com.ntg.securityaudit.enums.Severity;
import com.ntg.securityaudit.service.DashboardService;
import com.ntg.securityaudit.service.SiteService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

@Controller
public class HomeController {

    private final DashboardService dashboardService;
    private final SiteService siteService;

    public HomeController(DashboardService dashboardService,
                          SiteService siteService) {
        this.dashboardService = dashboardService;
        this.siteService = siteService;
    }

    @GetMapping("/")
    public String dashboard(@RequestParam(required = false) Long siteId,
                            @RequestParam(required = false) Integer auditRound,
                            @RequestParam(required = false) String category,
                            @RequestParam(required = false) Severity severity,
                            @RequestParam(required = false) FindingStatus status,
                            @RequestParam(required = false) Integer year,
                            Model model) {
        DashboardFilter filter = buildFilter(siteId, auditRound, category, severity, status, year);
        model.addAttribute("dashboard", dashboardService.getDashboardData(filter));
        model.addAttribute("filter", filter);
        model.addAttribute("sites", siteService.getAllSites());
        model.addAttribute("severities", Severity.values());
        model.addAttribute("statuses", FindingStatus.values());
        return "dashboard";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/dashboard/export/excel")
    public void exportDashboardCsv(@RequestParam(required = false) Long siteId,
                                   @RequestParam(required = false) Integer auditRound,
                                   @RequestParam(required = false) String category,
                                   @RequestParam(required = false) Severity severity,
                                   @RequestParam(required = false) FindingStatus status,
                                   @RequestParam(required = false) Integer year,
                                   HttpServletResponse response) throws IOException {
        DashboardDTO dashboard = dashboardService.getDashboardData(buildFilter(siteId, auditRound, category, severity, status, year));
        boolean vulnerabilityReport = isCategory(category, "Vulnerability");
        boolean complianceReport = isCategory(category, "Compliance");
        boolean combinedReport = !vulnerabilityReport && !complianceReport;
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=dashboard-export.csv");
        response.getWriter().println(exportTitle(category));
        response.getWriter().println();
        response.getWriter().println("Metric,Value");
        response.getWriter().println("Overall Security Score," + dashboard.getOverallSecurityScore());
        response.getWriter().println("Current Risk Level," + dashboard.getCurrentRiskLevel());
        response.getWriter().println("Total Sites," + dashboard.getTotalSites());
        response.getWriter().println("Total Audits," + dashboard.getTotalAudits());
        response.getWriter().println(totalLabel(category) + "," + dashboard.getTotalFindings());
        response.getWriter().println(openLabel(category) + "," + dashboard.getOpenFindings());
        response.getWriter().println(closedLabel(category) + "," + dashboard.getClosedFindings());
        response.getWriter().println("Accepted Risk " + entityLabel(category) + "," + dashboard.getAcceptedRiskFindings());
        response.getWriter().println("Overdue " + entityLabel(category) + "," + dashboard.getOverdueFindings());
        response.getWriter().println("Active Exceptions," + dashboard.getTotalActiveExceptions());
        response.getWriter().println("Expired Exceptions," + dashboard.getTotalExpiredExceptions());
        response.getWriter().println();

        if (combinedReport) {
            response.getWriter().println("Finding Type Summary");
            response.getWriter().println("Type,Count");
            writeSummaryRows(response, dashboard.getFindingTypeSummary());
            response.getWriter().println();
        }

        if (combinedReport || complianceReport) {
            response.getWriter().println("Compliance Status Summary");
            response.getWriter().println("Status,Count");
            writeSummaryRows(response, dashboard.getComplianceStatusSummary());
            response.getWriter().println();
        }

        if (combinedReport || vulnerabilityReport) {
            response.getWriter().println("Vulnerability Status Summary");
            response.getWriter().println("Status,Count");
            writeSummaryRows(response, dashboard.getVulnerabilityStatusSummary());
            response.getWriter().println();

            response.getWriter().println("Vulnerability Severity Summary");
            response.getWriter().println("Severity,Count");
            writeSummaryRows(response, dashboard.getVulnerabilitySeveritySummary());
            response.getWriter().println();
        }

        response.getWriter().println("Site,Latest Audit Score,Open " + entityLabel(category) + ",Critical " + entityLabel(category) + ",Overdue " + entityLabel(category) + ",Risk Level");
        for (DashboardDTO.SiteHeatmapItem item : dashboard.getSiteHeatmap()) {
            response.getWriter().println(csv(item.getSiteName()) + "," + item.getLatestAuditScore() + "," + item.getOpenFindings()
                    + "," + item.getCriticalFindings() + "," + item.getOverdueFindings() + "," + csv(item.getRiskLevel()));
        }
        response.getWriter().println();

        response.getWriter().println("Top Risks");
        response.getWriter().println("Risk,Category,Severity,Count");
        for (DashboardDTO.RiskItem item : dashboard.getTopRisks()) {
            response.getWriter().println(csv(item.getTitle()) + "," + csv(item.getCategory()) + "," + csv(item.getSeverity()) + "," + item.getCount());
        }
    }

    @GetMapping("/dashboard/export/pdf")
    public String exportDashboardPdf(@RequestParam(required = false) Long siteId,
                                     @RequestParam(required = false) Integer auditRound,
                                     @RequestParam(required = false) String category,
                                     @RequestParam(required = false) Severity severity,
                                     @RequestParam(required = false) FindingStatus status,
                                     @RequestParam(required = false) Integer year,
                                     Model model) {
        DashboardFilter filter = buildFilter(siteId, auditRound, category, severity, status, year);
        model.addAttribute("dashboard", dashboardService.getDashboardData(filter));
        model.addAttribute("filter", filter);
        model.addAttribute("exportTitle", exportTitle(category));
        model.addAttribute("totalLabel", totalLabel(category));
        model.addAttribute("openLabel", openLabel(category));
        model.addAttribute("closedLabel", closedLabel(category));
        model.addAttribute("entityLabel", entityLabel(category));
        model.addAttribute("includeFindingTypeSummary", !isCategory(category, "Vulnerability") && !isCategory(category, "Compliance"));
        model.addAttribute("includeComplianceSummary", !isCategory(category, "Vulnerability"));
        model.addAttribute("includeVulnerabilitySummary", !isCategory(category, "Compliance"));
        return "dashboard-export";
    }

    @GetMapping("/sites")
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN')")
    public String sites(Model model) {

        model.addAttribute("sites", siteService.getAllSites());

        return "sites";
    }

    @GetMapping("/settings")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public String settings() {
        return "settings";
    }

    private DashboardFilter buildFilter(Long siteId, Integer auditRound, String category, Severity severity, FindingStatus status, Integer year) {
        DashboardFilter filter = new DashboardFilter();
        filter.setSiteId(siteId);
        filter.setAuditRound(auditRound);
        filter.setCategory(category);
        filter.setSeverity(severity);
        filter.setStatus(status);
        filter.setYear(year);
        return filter;
    }

    private String csv(String value) {
        if (value == null) {
            return "";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private void writeSummaryRows(HttpServletResponse response, java.util.List<DashboardDTO.SummaryItem> items) throws IOException {
        for (DashboardDTO.SummaryItem item : items) {
            response.getWriter().println(csv(item.getLabel()) + "," + item.getCount());
        }
    }

    private boolean isCategory(String category, String expected) {
        return category != null && category.equalsIgnoreCase(expected);
    }

    private String exportTitle(String category) {
        if (isCategory(category, "Vulnerability")) {
            return "Firewall Security Vulnerability Report";
        }
        if (isCategory(category, "Compliance")) {
            return "Firewall Security Compliance Report";
        }
        return "Firewall Security Dashboard Report";
    }

    private String entityLabel(String category) {
        if (isCategory(category, "Vulnerability")) {
            return "Vulnerabilities";
        }
        if (isCategory(category, "Compliance")) {
            return "Compliances";
        }
        return "Findings";
    }

    private String totalLabel(String category) {
        return "Total " + entityLabel(category);
    }

    private String openLabel(String category) {
        return "Open " + entityLabel(category);
    }

    private String closedLabel(String category) {
        return "Closed " + entityLabel(category);
    }
}
