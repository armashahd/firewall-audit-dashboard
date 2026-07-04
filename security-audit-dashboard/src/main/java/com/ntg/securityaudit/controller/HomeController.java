package com.ntg.securityaudit.controller;

import com.ntg.securityaudit.entity.Audit;
import com.ntg.securityaudit.entity.Finding;
import com.ntg.securityaudit.entity.Site;
import com.ntg.securityaudit.service.DashboardService;
import com.ntg.securityaudit.service.AuditService;
import com.ntg.securityaudit.service.FindingService;
import com.ntg.securityaudit.service.SiteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private final DashboardService dashboardService;
    private final AuditService auditService;
    private final FindingService findingService;
    private final SiteService siteService;

    public HomeController(DashboardService dashboardService,
                          AuditService auditService,
                          FindingService findingService,
                          SiteService siteService) {
        this.dashboardService = dashboardService;
        this.auditService = auditService;
        this.findingService = findingService;
        this.siteService = siteService;
    }

    @GetMapping("/")
    public String dashboard(Model model) {

        var dashboard = dashboardService.getDashboardData();
        List<Site> sites = siteService.getAllSites();
        List<Audit> audits = auditService.getAllAudits();
        List<Finding> findings = findingService.getAllFindings();

        model.addAttribute("dashboard", dashboard);
        model.addAttribute("siteChartLabels", sites.stream().map(Site::getName).collect(Collectors.toList()));
        model.addAttribute("siteChartData", sites.stream().map(site -> site.getSecurityScore() == null ? 0 : site.getSecurityScore()).collect(Collectors.toList()));
        model.addAttribute("auditChartLabels", audits.stream().map(audit -> audit.getAuditDate() == null ? "N/A" : audit.getAuditDate().toString()).collect(Collectors.toList()));
        model.addAttribute("auditChartData", audits.stream().map(audit -> audit.getOverallScore() == null ? 0 : audit.getOverallScore()).collect(Collectors.toList()));
        model.addAttribute("findingSeverityLabels", List.of("Critical", "High", "Medium", "Low"));
        model.addAttribute("findingSeverityData", List.of(
                dashboard.getCriticalFindings(),
                dashboard.getHighFindings(),
                dashboard.getMediumFindings(),
                dashboard.getLowFindings()
        ));
        model.addAttribute("findingStatusLabels", List.of("Open", "Closed"));
        model.addAttribute("findingStatusData", List.of(
                dashboard.getOpenFindings(),
                dashboard.getClosedFindings()
        ));
        model.addAttribute("findingCount", findings.size());

        return "dashboard";
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
}
