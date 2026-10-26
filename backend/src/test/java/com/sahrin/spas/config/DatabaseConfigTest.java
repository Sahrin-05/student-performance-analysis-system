package com.sahrin.spas.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseConfigTest {

    @Test
    @DisplayName("Returns null or empty when input is null or blank")
    void testNullOrBlank() {
        assertNull(DatabaseConfig.normalizeJdbcUrl(null));
        assertEquals("", DatabaseConfig.normalizeJdbcUrl(""));
        assertEquals("   ", DatabaseConfig.normalizeJdbcUrl("   "));
    }

    @Test
    @DisplayName("Leaves standard JDBC URLs intact")
    void testJdbcUrlUnchanged() {
        String postgresJdbc = "jdbc:postgresql://localhost:5432/spas_db";
        assertEquals(postgresJdbc, DatabaseConfig.normalizeJdbcUrl(postgresJdbc));

        String h2Jdbc = "jdbc:h2:mem:spas_test_db;DB_CLOSE_DELAY=-1;MODE=PostgreSQL";
        assertEquals(h2Jdbc, DatabaseConfig.normalizeJdbcUrl(h2Jdbc));
    }

    @Test
    @DisplayName("Normalizes postgres:// URL with credentials and query parameters")
    void testNormalizePostgresSchemeWithCredentialsAndQuery() {
        String cloudUrl = "postgres://spas_user:secret123@aws-db.render.com:5432/spas_production?sslmode=require&prepareThreshold=0";
        String normalized = DatabaseConfig.normalizeJdbcUrl(cloudUrl);
        assertEquals("jdbc:postgresql://aws-db.render.com:5432/spas_production?sslmode=require&prepareThreshold=0", normalized);

        assertEquals("spas_user", DatabaseConfig.extractUsername(cloudUrl, "default_user"));
        assertEquals("secret123", DatabaseConfig.extractPassword(cloudUrl, "default_pass"));
    }

    @Test
    @DisplayName("Normalizes postgresql:// URL without port and without credentials")
    void testNormalizePostgresqlWithoutPort() {
        String cloudUrl = "postgresql://db.fly.io/spas_db";
        String normalized = DatabaseConfig.normalizeJdbcUrl(cloudUrl);
        assertEquals("jdbc:postgresql://db.fly.io/spas_db", normalized);

        assertEquals("fallback_user", DatabaseConfig.extractUsername(cloudUrl, "fallback_user"));
        assertEquals("fallback_pass", DatabaseConfig.extractPassword(cloudUrl, "fallback_pass"));
    }

    @Test
    @DisplayName("Extracts username when no password is present in userInfo")
    void testExtractUsernameWithoutPassword() {
        String cloudUrl = "postgres://onlyuser@localhost:5432/spas_db";
        assertEquals("onlyuser", DatabaseConfig.extractUsername(cloudUrl, "fallback"));
        assertEquals("fallback_pass", DatabaseConfig.extractPassword(cloudUrl, "fallback_pass"));
    }
}
