package com.ntg.securityaudit.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class DatabaseRepairService {

    private final JdbcTemplate jdbcTemplate;
    private final AtomicBoolean repaired = new AtomicBoolean(false);

    public DatabaseRepairService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void repairIfNeeded() {
        if (!repaired.compareAndSet(false, true)) {
            return;
        }

        jdbcTemplate.execute("DELETE FROM findings WHERE audit_id IS NULL OR audit_id NOT IN (SELECT id FROM audits)");
        jdbcTemplate.execute("DELETE FROM reports WHERE audit_id IS NULL OR audit_id NOT IN (SELECT id FROM audits)");
        jdbcTemplate.execute("DELETE FROM audit_exceptions WHERE audit_id IS NULL OR audit_id NOT IN (SELECT id FROM audits)");
        jdbcTemplate.execute("DELETE FROM audits WHERE site_id IS NULL OR site_id NOT IN (SELECT id FROM sites)");
        jdbcTemplate.execute("DELETE FROM findings WHERE audit_id IS NULL OR audit_id NOT IN (SELECT id FROM audits)");
        jdbcTemplate.execute("DELETE FROM reports WHERE audit_id IS NULL OR audit_id NOT IN (SELECT id FROM audits)");
        jdbcTemplate.execute("DELETE FROM audit_exceptions WHERE audit_id IS NULL OR audit_id NOT IN (SELECT id FROM audits)");
    }
}
