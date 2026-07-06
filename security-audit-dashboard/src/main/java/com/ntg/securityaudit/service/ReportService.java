package com.ntg.securityaudit.service;

import com.ntg.securityaudit.entity.Report;
import com.ntg.securityaudit.repository.ReportRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final DatabaseRepairService databaseRepairService;

    public ReportService(ReportRepository reportRepository, DatabaseRepairService databaseRepairService) {
        this.reportRepository = reportRepository;
        this.databaseRepairService = databaseRepairService;
    }

    public List<Report> getAllReports() {
        databaseRepairService.repairIfNeeded();
        return reportRepository.findAll();
    }

    public Report getReportById(Long id) {
        databaseRepairService.repairIfNeeded();
        return reportRepository.findById(id).orElse(null);
    }

    public Report saveReport(Report report) {
        return reportRepository.save(report);
    }

    public void deleteReport(Long id) {
        if (id != null && reportRepository.existsById(id)) {
            reportRepository.deleteById(id);
        }
    }
}
