package com.sahrin.spas.migration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class FlywayMigrationTest {

    private DataSource createH2DataSource(String dbName) {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:" + dbName + ";DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
        ds.setUser("sa");
        ds.setPassword("");
        return ds;
    }

    @Test
    @DisplayName("Clean install: Flyway runs V1 and V2 on empty database")
    void testCleanDatabaseMigration() throws Exception {
        DataSource ds = createH2DataSource("flyway_clean_test");

        Flyway flyway = Flyway.configure()
                .dataSource(ds)
                .locations("classpath:db/migration")
                .load();

        MigrateResult result = flyway.migrate();
        assertTrue(result.success, "Flyway migration should succeed on clean install");
        assertEquals(2, result.migrationsExecuted, "V1 and V2 should execute on a clean install");

        try (Connection conn = ds.getConnection(); Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
                assertTrue(rs.next());
                assertEquals(0, rs.getInt(1));
            }
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM admin_audit_logs")) {
                assertTrue(rs.next());
                assertEquals(0, rs.getInt(1));
            }
        }
    }

    @Test
    @DisplayName("Existing database: Flyway baselines at 1, preserves existing data, and applies V2")
    void testExistingDatabaseBaselineAndAdoption() throws Exception {
        DataSource ds = createH2DataSource("flyway_existing_test");

        // Simulate pre-existing database with legacy user table and data
        try (Connection conn = ds.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE users (
                    id BIGSERIAL PRIMARY KEY,
                    full_name VARCHAR(100) NOT NULL,
                    identifier VARCHAR(50) NOT NULL,
                    email VARCHAR(100) NOT NULL,
                    password_hash VARCHAR(255) NOT NULL,
                    role VARCHAR(20) NOT NULL,
                    status VARCHAR(30) NOT NULL
                )
            """);
            stmt.execute("""
                INSERT INTO users (full_name, identifier, email, password_hash, role, status)
                VALUES ('Dr. Existing Lecturer', 'LEC-EXIST-01', 'existing@spas.edu', 'hash', 'LECTURER', 'ACTIVE')
            """);
        }

        // Configure Flyway as in application-prod.properties
        Flyway flyway = Flyway.configure()
                .dataSource(ds)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .baselineVersion("1")
                .baselineDescription("Initial baseline for existing database")
                .load();

        MigrateResult result = flyway.migrate();
        assertTrue(result.success, "Flyway adoption should succeed");
        assertEquals(1, result.migrationsExecuted, "Only V2 should execute because V1 was baselined");

        // Verify existing data is preserved untouched
        try (Connection conn = ds.getConnection(); Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery("SELECT full_name, email FROM users WHERE identifier = 'LEC-EXIST-01'")) {
                assertTrue(rs.next(), "Existing user record must still exist");
                assertEquals("Dr. Existing Lecturer", rs.getString("full_name"));
                assertEquals("existing@spas.edu", rs.getString("email"));
            }

            // Verify newly created admin_audit_logs table exists and accepts writes
            stmt.execute("""
                INSERT INTO admin_audit_logs (action, entity_type, entity_id, entity_identifier, details, performed_by_id, performed_by_email, performed_at)
                VALUES ('TEST_ACTION', 'USER', 1, 'LEC-EXIST-01', 'Test audit log details', 1, 'existing@spas.edu', CURRENT_TIMESTAMP)
            """);

            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM admin_audit_logs")) {
                assertTrue(rs.next());
                assertEquals(1, rs.getInt(1));
            }
        }
    }

    @Test
    @DisplayName("Idempotency: V2 safely no-ops when admin_audit_logs already exists")
    void testIdempotentMigrationWhenTableAlreadyExists() throws Exception {
        DataSource ds = createH2DataSource("flyway_idempotent_test");

        // Simulate pre-existing database that already has both users and admin_audit_logs
        try (Connection conn = ds.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE users (
                    id BIGSERIAL PRIMARY KEY,
                    full_name VARCHAR(100) NOT NULL,
                    identifier VARCHAR(50) NOT NULL,
                    email VARCHAR(100) NOT NULL,
                    password_hash VARCHAR(255) NOT NULL,
                    role VARCHAR(20) NOT NULL,
                    status VARCHAR(30) NOT NULL
                )
            """);
            stmt.execute("""
                CREATE TABLE admin_audit_logs (
                    id BIGSERIAL PRIMARY KEY,
                    action VARCHAR(50) NOT NULL,
                    entity_type VARCHAR(50) NOT NULL,
                    entity_id BIGINT,
                    entity_identifier VARCHAR(100),
                    details TEXT NOT NULL,
                    performed_by_id BIGINT REFERENCES users(id),
                    performed_by_email VARCHAR(100) NOT NULL,
                    performed_at TIMESTAMP NOT NULL
                )
            """);
        }

        Flyway flyway = Flyway.configure()
                .dataSource(ds)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .baselineVersion("1")
                .load();

        MigrateResult result = flyway.migrate();
        assertTrue(result.success, "Migration should succeed idempotently");
        assertEquals(1, result.migrationsExecuted, "V2 executed safely without error");
    }

    @Test
    @DisplayName("Schema mismatch detection: missing indexed columns cause Flyway migration failure")
    void testPreExistingTableMissingIndexedColumnFailsFlyway() {
        DataSource ds = createH2DataSource("flyway_missing_col_test");

        // Simulate an existing database that has an incompatible legacy admin_audit_logs table missing 'entity_id'
        try (Connection conn = ds.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE admin_audit_logs (
                    id BIGSERIAL PRIMARY KEY,
                    action VARCHAR(50) NOT NULL,
                    entity_type VARCHAR(50) NOT NULL
                )
            """);
        } catch (Exception e) {
            fail("Failed to setup test database: " + e.getMessage());
        }

        Flyway flyway = Flyway.configure()
                .dataSource(ds)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .baselineVersion("1")
                .load();

        // Migration fails because the pre-existing table lacks the required 'entity_id' column for idx_admin_audit_entity
        assertThrows(Exception.class, flyway::migrate,
                "Flyway migration should fail when pre-existing table lacks columns required by indexes");
    }

    @Test
    @DisplayName("Schema mismatch limitation: IF NOT EXISTS does not repair unindexed columns like 'details'")
    void testPreExistingTableMissingUnindexedColumnIsNotRepairedByIfNotExists() throws Exception {
        DataSource ds = createH2DataSource("flyway_missing_details_test");

        // Simulate an existing database with indexed columns present, but missing unindexed 'details' column
        try (Connection conn = ds.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE admin_audit_logs (
                    id BIGSERIAL PRIMARY KEY,
                    action VARCHAR(50) NOT NULL,
                    entity_type VARCHAR(50) NOT NULL,
                    entity_id BIGINT,
                    performed_at TIMESTAMP NOT NULL
                )
            """);
        }

        Flyway flyway = Flyway.configure()
                .dataSource(ds)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .baselineVersion("1")
                .load();

        MigrateResult result = flyway.migrate();
        assertTrue(result.success, "Flyway succeeds because all indexed columns exist and CREATE TABLE IF NOT EXISTS skips the table");

        // Prove that IF NOT EXISTS did NOT add the missing 'details' column (proving IF NOT EXISTS is not a schema validator)
        try (Connection conn = ds.getConnection()) {
            var meta = conn.getMetaData();
            try (ResultSet rs = meta.getColumns(null, "PUBLIC", "ADMIN_AUDIT_LOGS", "DETAILS")) {
                assertFalse(rs.next(), "Column 'details' was NOT added by IF NOT EXISTS — verifying that manual check or Hibernate validation is required");
            }
        }
    }
}
