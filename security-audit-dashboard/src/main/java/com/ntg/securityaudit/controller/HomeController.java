package com.ntg.securityaudit.controller;

import com.ntg.securityaudit.service.DashboardService;
import com.ntg.securityaudit.service.SiteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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
    public String dashboard(Model model) {
        model.addAttribute("dashboard", dashboardService.getDashboardData());
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
