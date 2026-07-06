package com.ntg.securityaudit.controller;

import com.ntg.securityaudit.dto.DashboardDTO;
import com.ntg.securityaudit.dto.DashboardFilter;
import com.ntg.securityaudit.enums.FindingStatus;
import com.ntg.securityaudit.enums.Severity;
import com.ntg.securityaudit.service.DashboardService;
import com.ntg.securityaudit.service.SiteService;
import jakarta.servlet.http.HttpServletResponse;
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

    @GetMapping("/dashboard/export/excel")
    public void exportDashboardCsv(@RequestParam(required = false) Long siteId,
                                   @RequestParam(required = false) Integer auditRound,
                                   @RequestParam(required = false) String category,
                                   @RequestParam(required = false) Severity severity,
                                   @RequestParam(required = false) FindingStatus status,
                                   @RequestParam(required = false) Integer year,
                                   HttpServletResponse response) throws IOException {
        DashboardDTO dashboard = dashboardService.getDashboardData(buildFilter(siteId, auditRound, category, severity, status, year));
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=dashboard-export.csv");
        response.getWriter().println("Metric,Value");
        response.getWriter().println("Overall Security Score," + dashboard.getOverallSecurityScore());
        response.getWriter().println("Current Risk Level," + dashboard.getCurrentRiskLevel());
        response.getWriter().println("Total Sites," + dashboard.getTotalSites());
        response.getWriter().println("Total Audits," + dashboard.getTotalAudits());
        response.getWriter().println("Total Findings," + dashboard.getTotalFindings());
        response.getWriter().println("Open Findings," + dashboard.getOpenFindings());
        response.getWriter().println("Closed Findings," + dashboard.getClosedFindings());
        response.getWriter().println("Accepted Risk Findings," + dashboard.getAcceptedRiskFindings());
        response.getWriter().println("Overdue Findings," + dashboard.getOverdueFindings());
        response.getWriter().println("Active Exceptions," + dashboard.getTotalActiveExceptions());
        response.getWriter().println("Expired Exceptions," + dashboard.getTotalExpiredExceptions());
        response.getWriter().println();
        response.getWriter().println("Site,Latest Audit Score,Open Findings,Critical Findings,Overdue Findings,Risk Level");
        for (DashboardDTO.SiteHeatmapItem item : dashboard.getSiteHeatmap()) {
            response.getWriter().println(csv(item.getSiteName()) + "," + item.getLatestAuditScore() + "," + item.getOpenFindings()
                    + "," + item.getCriticalFindings() + "," + item.getOverdueFindings() + "," + item.getRiskLevel());
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
        return "dashboard-export";
    }

    @GetMapping("/sites")
    public String sites(Model model) {

        model.addAttribute("sites", siteService.getAllSites());

        return "sites";
    }

    @GetMapping("/settings")
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
}
