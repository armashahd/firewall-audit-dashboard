package com.ntg.securityaudit.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DatabaseRepairService {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseRepairService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void repairIfNeeded() {
        jdbcTemplate.execute("DELETE FROM findings WHERE audit_id IS NULL OR audit_id NOT IN (SELECT id FROM audits)");
        jdbcTemplate.execute("DELETE FROM reports WHERE audit_id IS NULL OR audit_id NOT IN (SELECT id FROM audits)");
        jdbcTemplate.execute("DELETE FROM audit_exceptions WHERE audit_id IS NULL OR audit_id NOT IN (SELECT id FROM audits)");
        jdbcTemplate.execute("DELETE FROM audit_exceptions WHERE finding_id IS NOT NULL AND finding_id NOT IN (SELECT id FROM findings)");
        jdbcTemplate.execute("DELETE FROM audits WHERE site_id IS NULL OR site_id NOT IN (SELECT id FROM sites)");
        jdbcTemplate.execute("DELETE FROM findings WHERE audit_id IS NULL OR audit_id NOT IN (SELECT id FROM audits)");
        jdbcTemplate.execute("DELETE FROM reports WHERE audit_id IS NULL OR audit_id NOT IN (SELECT id FROM audits)");
        jdbcTemplate.execute("DELETE FROM audit_exceptions WHERE audit_id IS NULL OR audit_id NOT IN (SELECT id FROM audits)");
        jdbcTemplate.execute("DELETE FROM audit_exceptions WHERE finding_id IS NOT NULL AND finding_id NOT IN (SELECT id FROM findings)");
    }
}
