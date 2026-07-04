package com.ntg.securityaudit.service;

import com.ntg.securityaudit.entity.Audit;
import com.ntg.securityaudit.repository.AuditRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditService {

    private final AuditRepository auditRepository;

    public AuditService(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    public List<Audit> getAllAudits() {
        return auditRepository.findAll();
    }

    public Audit getAuditById(Long id) {
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
