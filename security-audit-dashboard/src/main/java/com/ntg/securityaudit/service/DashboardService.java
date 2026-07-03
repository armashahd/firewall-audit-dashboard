package com.ntg.securityaudit.service;

import com.ntg.securityaudit.dto.DashboardDTO;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    public DashboardDTO getDashboardData() {

        DashboardDTO dto = new DashboardDTO();

        dto.setOverallSecurityScore(55);
        dto.setTotalSites(10L);
        dto.setOpenFindings(15L);
        dto.setClosedFindings(48L);
        dto.setCriticalFindings(2L);
        dto.setHighFindings(5L);
        dto.setMediumFindings(8L);
        dto.setLowFindings(33L);
        dto.setCompletionPercentage(76);

        return dto;
    }
}