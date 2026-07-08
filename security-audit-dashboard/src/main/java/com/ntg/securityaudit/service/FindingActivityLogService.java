package com.ntg.securityaudit.service;

import com.ntg.securityaudit.entity.FindingActivityLog;
import com.ntg.securityaudit.repository.FindingActivityLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FindingActivityLogService {

    private final FindingActivityLogRepository findingActivityLogRepository;
    private final DatabaseRepairService databaseRepairService;

    public FindingActivityLogService(FindingActivityLogRepository findingActivityLogRepository,
                                     DatabaseRepairService databaseRepairService) {
        this.findingActivityLogRepository = findingActivityLogRepository;
        this.databaseRepairService = databaseRepairService;
    }

    public List<FindingActivityLog> getLogsForFinding(Long findingId) {
        databaseRepairService.repairIfNeeded();
        return findingActivityLogRepository.findByFindingIdOrderByCreatedAtDesc(findingId);
    }
}
