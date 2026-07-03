package com.ntg.securityaudit.controller;

import com.ntg.securityaudit.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/")
    public String dashboard(Model model) {

        model.addAttribute("dashboard", dashboardService.getDashboardData());

        return "dashboard";
    }

    @GetMapping("/sites")
    public String sites() {
        return "sites";
    }

    @GetMapping("/findings")
    public String findings() {
        return "findings";
    }

    @GetMapping("/reports")
    public String reports() {
        return "reports";
    }

    @GetMapping("/settings")
    public String settings() {
        return "settings";
    }
}