package com.ntg.securityaudit.service;

import com.ntg.securityaudit.entity.Site;
import com.ntg.securityaudit.repository.SiteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SiteService {

    private final SiteRepository siteRepository;

    public SiteService(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    public List<Site> getAllSites() {
        return siteRepository.findAll();
    }

    public Site saveSite(Site site) {
        return siteRepository.save(site);
    }

    public Site getSiteById(Long id) {
        return siteRepository.findById(id).orElse(null);
    }

    public void deleteSite(Long id) {
        siteRepository.deleteById(id);
    }

}