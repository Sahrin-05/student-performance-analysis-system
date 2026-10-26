package com.sahrin.spas.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Normalizes database URLs, especially cloud provider DATABASE_URL formats
 * such as postgres:// or postgresql:// into standard JDBC URLs.
 */
@Configuration
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Bean
    @Primary
    @Profile("!test")
    public DataSource dataSource(
            @Value("${spring.datasource.url:}") String rawUrl,
            @Value("${spring.datasource.username:}") String rawUsername,
            @Value("${spring.datasource.password:}") String rawPassword,
            @Value("${spring.datasource.driver-class-name:}") String driverClassName
    ) {
        String normalizedUrl = normalizeJdbcUrl(rawUrl);
        String username = extractUsername(rawUrl, rawUsername);
        String password = extractPassword(rawUrl, rawPassword);

        if ((driverClassName == null || driverClassName.isEmpty()) && normalizedUrl != null && normalizedUrl.startsWith("jdbc:postgresql:")) {
            driverClassName = "org.postgresql.Driver";
        }

        DataSourceBuilder<?> builder = DataSourceBuilder.create()
                .url(normalizedUrl)
                .username(username)
                .password(password);

        if (driverClassName != null && !driverClassName.isEmpty()) {
            builder.driverClassName(driverClassName);
        }

        return builder.build();
    }

    /**
     * Normalizes a raw database connection string into a valid JDBC PostgreSQL URL.
     * Preserves JDBC URLs as-is (including H2, PostgreSQL, etc.).
     * Converts postgres:// and postgresql:// URIs into jdbc:postgresql://...
     * Preserves host, port, database name, and query parameters (e.g. sslmode).
     *
     * @param rawUrl the database URL from environment or configuration
     * @return normalized JDBC URL, or rawUrl if null/blank or already a JDBC URL
     */
    public static String normalizeJdbcUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.trim().isEmpty()) {
            return rawUrl;
        }

        String trimmed = rawUrl.trim();

        // Already a standard JDBC URL
        if (trimmed.startsWith("jdbc:")) {
            return trimmed;
        }

        // Check for postgres:// or postgresql:// scheme
        if (trimmed.startsWith("postgres://") || trimmed.startsWith("postgresql://")) {
            try {
                URI uri = new URI(trimmed);
                String host = uri.getHost();
                int port = uri.getPort();
                String path = uri.getPath();
                String query = uri.getQuery();

                if (host == null) {
                    log.warn("Unable to parse host from database URI: {}", trimmed);
                    return trimmed;
                }

                StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://");
                jdbcUrl.append(host);
                if (port > 0) {
                    jdbcUrl.append(":").append(port);
                }

                if (path != null && !path.isEmpty()) {
                    if (!path.startsWith("/")) {
                        jdbcUrl.append("/");
                    }
                    jdbcUrl.append(path);
                }

                if (query != null && !query.isEmpty()) {
                    jdbcUrl.append("?").append(query);
                }

                return jdbcUrl.toString();
            } catch (URISyntaxException e) {
                log.warn("Failed to parse database URI '{}', falling back to raw URL: {}", trimmed, e.getMessage());
                return trimmed;
            }
        }

        return trimmed;
    }

    /**
     * Extracts the username from a postgres:// or postgresql:// URI if present,
     * otherwise returns the provided fallback username.
     */
    public static String extractUsername(String rawUrl, String fallbackUsername) {
        if (rawUrl == null || rawUrl.trim().isEmpty()) {
            return fallbackUsername;
        }
        String trimmed = rawUrl.trim();
        if (trimmed.startsWith("postgres://") || trimmed.startsWith("postgresql://")) {
            try {
                URI uri = new URI(trimmed);
                String userInfo = uri.getUserInfo();
                if (userInfo != null && !userInfo.isEmpty()) {
                    int colonIndex = userInfo.indexOf(':');
                    return colonIndex > -1 ? userInfo.substring(0, colonIndex) : userInfo;
                }
            } catch (URISyntaxException ignored) {
            }
        }
        return fallbackUsername;
    }

    /**
     * Extracts the password from a postgres:// or postgresql:// URI if present,
     * otherwise returns the provided fallback password.
     */
    public static String extractPassword(String rawUrl, String fallbackPassword) {
        if (rawUrl == null || rawUrl.trim().isEmpty()) {
            return fallbackPassword;
        }
        String trimmed = rawUrl.trim();
        if (trimmed.startsWith("postgres://") || trimmed.startsWith("postgresql://")) {
            try {
                URI uri = new URI(trimmed);
                String userInfo = uri.getUserInfo();
                if (userInfo != null && !userInfo.isEmpty()) {
                    int colonIndex = userInfo.indexOf(':');
                    if (colonIndex > -1 && colonIndex < userInfo.length() - 1) {
                        return userInfo.substring(colonIndex + 1);
                    }
                }
            } catch (URISyntaxException ignored) {
            }
        }
        return fallbackPassword;
    }
}
