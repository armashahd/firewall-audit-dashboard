package com.ntg.securityaudit.service;

import com.ntg.securityaudit.entity.Audit;
import com.ntg.securityaudit.repository.AuditRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditService {

    private final AuditRepository auditRepository;
    private final DatabaseRepairService databaseRepairService;

    public AuditService(AuditRepository auditRepository, DatabaseRepairService databaseRepairService) {
        this.auditRepository = auditRepository;
        this.databaseRepairService = databaseRepairService;
    }

    public List<Audit> getAllAudits() {
        databaseRepairService.repairIfNeeded();
        return auditRepository.findAll();
    }

    public Audit getAuditById(Long id) {
        databaseRepairService.repairIfNeeded();
        return auditRepository.findById(id).orElse(null);
    }

    public Audit saveAudit(Audit audit) {
        return auditRepository.save(audit);
    }

    public void deleteAudit(Long id) {
        if (id != null && auditRepository.existsById(id)) {
            auditRepository.deleteById(id);
        }
    }
}
