package com.sahrin.spas.config;

import com.sahrin.spas.entity.Role;
import com.sahrin.spas.entity.User;
import com.sahrin.spas.entity.User.UserStatus;
import com.sahrin.spas.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Automatically seeds an initial Administrator account if configured via environment variables.
 * Safe and idempotent: Skips execution if required environment variables are missing or if an
 * Administrator account already exists in the system.
 */
@Component
public class AdminInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;
    private final String adminIdentifier;
    private final String adminFullName;

    public AdminInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.email:}") String adminEmail,
            @Value("${app.admin.password:}") String adminPassword,
            @Value("${app.admin.identifier:ADMIN001}") String adminIdentifier,
            @Value("${app.admin.full-name:System Administrator}") String adminFullName
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
        this.adminIdentifier = adminIdentifier;
        this.adminFullName = adminFullName;
    }

    @Override
    public void run(String... args) {
        // 1. Check if required configuration is present
        if (adminEmail == null || adminEmail.trim().isEmpty() || adminPassword == null || adminPassword.trim().isEmpty()) {
            log.info("Initial Admin seeding skipped: ADMIN_EMAIL or ADMIN_PASSWORD environment variables are not set. "
                    + "To initialize a default administrator account, set ADMIN_EMAIL and ADMIN_PASSWORD in your environment.");
            return;
        }

               String normalizedEmail = adminEmail.trim().toLowerCase();
        String normalizedIdentifier = (adminIdentifier != null && !adminIdentifier.trim().isEmpty())
                ? adminIdentifier.trim()
                : "ADMIN001";
        String normalizedFullName = (adminFullName != null && !adminFullName.trim().isEmpty())
                ? adminFullName.trim()
                : "System Administrator";

        // 2. Idempotency Check: Account with configured email already exists
        if (userRepository.existsByEmail(normalizedEmail)) {
            log.info("Initial Admin seeding skipped: An account with email '{}' already exists in the system.", normalizedEmail);
            return;
        }

        // 3. Idempotency Check: Account with configured identifier already exists
        if (userRepository.existsByIdentifier(normalizedIdentifier)) {
            log.info("Initial Admin seeding skipped: An account with identifier '{}' already exists in the system.", normalizedIdentifier);
            return;
        }

        // 4. Idempotency Check: An account with ADMIN role already exists in the database
        if (userRepository.existsByRole(Role.ADMIN)) {
            log.info("Initial Admin seeding skipped: An administrator account already exists in the database.");
            return;
        }

        // 5. Create initial Admin with BCrypt hashed password, role ADMIN, and status ACTIVE
        String hashedPassword = passwordEncoder.encode(adminPassword.trim());
        User adminUser = new User(
                normalizedFullName,
                normalizedIdentifier,
                normalizedEmail,
                hashedPassword,
                Role.ADMIN,
                UserStatus.ACTIVE
        );

        userRepository.save(adminUser);
        log.info("Initial Administrator account created successfully with email '{}' and identifier '{}' (Role: {}, Status: {}).",
                normalizedEmail, normalizedIdentifier, Role.ADMIN, UserStatus.ACTIVE);
    }
}
