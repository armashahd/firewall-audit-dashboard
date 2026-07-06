package com.ntg.securityaudit.service;

import com.ntg.securityaudit.entity.Finding;
import com.ntg.securityaudit.repository.FindingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FindingService {

    private final FindingRepository findingRepository;
    private final DatabaseRepairService databaseRepairService;

    public FindingService(FindingRepository findingRepository, DatabaseRepairService databaseRepairService) {
        this.findingRepository = findingRepository;
        this.databaseRepairService = databaseRepairService;
    }

    public List<Finding> getAllFindings() {
        databaseRepairService.repairIfNeeded();
        return findingRepository.findAll();
    }

    public Finding getFindingById(Long id) {
        databaseRepairService.repairIfNeeded();
        return findingRepository.findById(id).orElse(null);
    }

    public Finding saveFinding(Finding finding) {
        return findingRepository.save(finding);
    }

    public void deleteFinding(Long id) {
        if (id != null && findingRepository.existsById(id)) {
            findingRepository.deleteById(id);
        }
    }
}
