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
        jdbcTemplate.execute("DROP TABLE IF EXISTS stale_imported_audits");
        jdbcTemplate.execute("CREATE TEMP TABLE stale_imported_audits AS "
                + "SELECT a.id, a.site_id FROM audits a "
                + "WHERE a.remarks LIKE 'Imported from uploaded firewall audit report%' "
                + "AND NOT EXISTS (SELECT 1 FROM reports r WHERE r.audit_id = a.id)");
        jdbcTemplate.execute("DELETE FROM finding_activity_logs WHERE finding_id IN "
                + "(SELECT f.id FROM findings f JOIN stale_imported_audits s ON s.id = f.audit_id)");
        jdbcTemplate.execute("DELETE FROM activity_logs WHERE entity_type = 'Finding' AND entity_id IN "
                + "(SELECT CAST(f.id AS TEXT) FROM findings f JOIN stale_imported_audits s ON s.id = f.audit_id)");
        jdbcTemplate.execute("DELETE FROM activity_logs WHERE entity_type = 'Audit Exception' AND entity_id IN "
                + "(SELECT CAST(e.id AS TEXT) FROM audit_exceptions e JOIN stale_imported_audits s ON s.id = e.audit_id)");
        jdbcTemplate.execute("DELETE FROM activity_logs WHERE entity_type = 'Audit' AND entity_id IN "
                + "(SELECT CAST(id AS TEXT) FROM stale_imported_audits)");
        jdbcTemplate.execute("DELETE FROM audit_exceptions WHERE audit_id IN (SELECT id FROM stale_imported_audits) "
                + "OR finding_id IN (SELECT f.id FROM findings f JOIN stale_imported_audits s ON s.id = f.audit_id)");
        jdbcTemplate.execute("DELETE FROM findings WHERE audit_id IN (SELECT id FROM stale_imported_audits)");
        jdbcTemplate.execute("DELETE FROM audits WHERE id IN (SELECT id FROM stale_imported_audits)");
        jdbcTemplate.execute("DELETE FROM activity_logs WHERE entity_type = 'Site' AND entity_id IN "
                + "(SELECT CAST(site_id AS TEXT) FROM stale_imported_audits s "
                + "WHERE s.site_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM audits a WHERE a.site_id = s.site_id))");
        jdbcTemplate.execute("DELETE FROM sites WHERE id IN "
                + "(SELECT site_id FROM stale_imported_audits s "
                + "WHERE s.site_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM audits a WHERE a.site_id = s.site_id))");
        jdbcTemplate.execute("DROP TABLE IF EXISTS stale_imported_audits");

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
