package com.ntg.securityaudit.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String dashboard() {
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