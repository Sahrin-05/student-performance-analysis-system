-- ===================================================================
-- SPAS Migration V2: System Governance & Admin Audit Trail
--
-- PURPOSE:
-- Delta migration for pre-existing databases baselined at version 1
-- where admin_audit_logs does not yet exist.
-- On clean installations, V1 already created this table; therefore,
-- CREATE TABLE IF NOT EXISTS safely no-ops.
--
-- IMMUTABILITY & EVOLUTION POLICY:
-- Do NOT alter or delete this migration once deployed.
-- All future schema modifications (new columns, index adjustments)
-- must be authored as subsequent versioned migrations (e.g. V3__...).
-- ===================================================================

CREATE TABLE IF NOT EXISTS admin_audit_logs (
    id BIGSERIAL PRIMARY KEY,
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT,
    entity_identifier VARCHAR(100),
    details TEXT NOT NULL,
    performed_by_id BIGINT REFERENCES users(id),
    performed_by_email VARCHAR(100) NOT NULL,
    performed_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_admin_audit_action ON admin_audit_logs(action);
CREATE INDEX IF NOT EXISTS idx_admin_audit_entity ON admin_audit_logs(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_admin_audit_performed_at ON admin_audit_logs(performed_at);
