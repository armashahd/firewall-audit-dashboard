package com.ntg.securityaudit.controller;

import com.ntg.securityaudit.entity.Site;
import com.ntg.securityaudit.enums.SiteStatus;
import com.ntg.securityaudit.service.SiteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class SiteController {

    private final SiteService siteService;

    public SiteController(SiteService siteService) {
        this.siteService = siteService;
    }

    @GetMapping("/sites/new")
    public String showAddSiteForm(Model model) {

        model.addAttribute("site", new Site());
        model.addAttribute("formTitle", "Add New Site");
        model.addAttribute("submitLabel", "Save Site");

        return "site-form";
    }

    @GetMapping("/sites/edit/{id}")
    public String showEditSiteForm(@PathVariable Long id, Model model) {

        Site site = siteService.getSiteById(id);

        if (site == null) {
            return "redirect:/sites";
        }

        model.addAttribute("site", site);
        model.addAttribute("formTitle", "Edit Site");
        model.addAttribute("submitLabel", "Update Site");

        return "site-form";
    }

    @PostMapping("/sites")
    public String saveSite(@ModelAttribute Site site) {

        if (site.getId() != null) {
            Site existingSite = siteService.getSiteById(site.getId());

            if (existingSite != null) {
                site.setStatus(existingSite.getStatus());
                site.setLastAuditDate(existingSite.getLastAuditDate());
            }
        }

        if (site.getStatus() == null) {
            site.setStatus(SiteStatus.ACTIVE);
        }

        siteService.saveSite(site);

        return "redirect:/sites";
    }
}
