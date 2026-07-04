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
import org.springframework.util.StringUtils;

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

    @GetMapping("/sites/details/{id}")
    public String showSiteDetails(@PathVariable Long id, Model model) {

        Site site = siteService.getSiteById(id);

        if (site == null) {
            return "redirect:/sites";
        }

        model.addAttribute("site", site);

        return "site-details";
    }

    @PostMapping("/sites")
    public String saveSite(@ModelAttribute Site site, Model model) {

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

        if (!isValidSite(site, model)) {
            model.addAttribute("formTitle", site.getId() == null ? "Add New Site" : "Edit Site");
            model.addAttribute("submitLabel", site.getId() == null ? "Save Site" : "Update Site");
            return "site-form";
        }

        siteService.saveSite(site);

        return "redirect:/sites";
    }

    @PostMapping("/sites/{id}/delete")
    public String deleteSite(@PathVariable Long id) {
        siteService.deleteSite(id);
        return "redirect:/sites";
    }

    private boolean isValidSite(Site site, Model model) {
        boolean valid = true;

        if (!StringUtils.hasText(site.getName())) {
            model.addAttribute("nameError", "Site name is required.");
            valid = false;
        }

        if (!StringUtils.hasText(site.getCountry())) {
            model.addAttribute("countryError", "Country is required.");
            valid = false;
        }

        if (!StringUtils.hasText(site.getCity())) {
            model.addAttribute("cityError", "City is required.");
            valid = false;
        }

        if (!StringUtils.hasText(site.getFirewallVendor())) {
            model.addAttribute("firewallVendorError", "Firewall vendor is required.");
            valid = false;
        }

        if (!StringUtils.hasText(site.getFirewallModel())) {
            model.addAttribute("firewallModelError", "Firewall model is required.");
            valid = false;
        }

        if (!StringUtils.hasText(site.getFirmwareVersion())) {
            model.addAttribute("firmwareVersionError", "Firmware version is required.");
            valid = false;
        }

        if (site.getSecurityScore() == null) {
            model.addAttribute("securityScoreError", "Security score is required.");
            valid = false;
        } else if (site.getSecurityScore() < 0 || site.getSecurityScore() > 100) {
            model.addAttribute("securityScoreError", "Security score must be between 0 and 100.");
            valid = false;
        }

        if (!valid) {
            model.addAttribute("errorMessage", "Please correct the highlighted fields.");
        }

        return valid;
    }
}
