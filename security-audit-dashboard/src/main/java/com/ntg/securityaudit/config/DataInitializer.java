package com.ntg.securityaudit.config;

import com.ntg.securityaudit.repository.SiteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.ntg.securityaudit.entity.Site;
import com.ntg.securityaudit.enums.SiteStatus;
import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private final SiteRepository siteRepository;

    public DataInitializer(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    @Override
    public void run(String... args) {

        if (siteRepository.count() > 0) {
            return;
        }

        System.out.println("Database is empty. Sample data will be inserted...");

        Site site = new Site();

        site.setName("Colombo HQ");
        site.setCountry("Sri Lanka");
        site.setCity("Colombo");
        site.setFirewallVendor("Fortinet");
        site.setFirewallModel("FortiGate 100F");
        site.setFirmwareVersion("7.4.8");
        site.setLastAuditDate(LocalDate.now());
        site.setSecurityScore(92);
        site.setStatus(SiteStatus.ACTIVE);

        siteRepository.save(site);

        System.out.println("Sample site inserted successfully.");
    }
}