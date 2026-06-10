package io.github.springwhale.rbac.config;

import io.github.springwhale.rbac.entity.RoleEntity;
import io.github.springwhale.rbac.entity.UserEntity;
import io.github.springwhale.rbac.entity.UserRoleEntity;
import io.github.springwhale.rbac.repository.RoleRepository;
import io.github.springwhale.rbac.repository.UserRepository;
import io.github.springwhale.rbac.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Initializes a default super-admin account on application startup.
 * <p>
 * If the admin user does not exist, it creates one with username "admin"
 * and password "admin", along with a SUPER_ADMIN role. If the user already
 * exists, no changes are made.
 * </p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminInitializer implements CommandLineRunner {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin";
    private static final String ADMIN_REAL_NAME = "Super Administrator";
    private static final String SUPER_ADMIN_ROLE_CODE = "SUPER_ADMIN";
    private static final String SUPER_ADMIN_ROLE_NAME = "Super Administrator";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String @NonNull ... args) {
        // Ensure SUPER_ADMIN role exists
        RoleEntity superAdminRole = roleRepository.findByCode(SUPER_ADMIN_ROLE_CODE)
                .orElseGet(() -> {
                    RoleEntity role = new RoleEntity();
                    role.setCode(SUPER_ADMIN_ROLE_CODE);
                    role.setName(SUPER_ADMIN_ROLE_NAME);
                    role.setDescription("Built-in super administrator role with full permissions");
                    role.setStatus(1);
                    role.setSort(0);
                    return roleRepository.save(role);
                });

        UserEntity admin = userRepository.findByUsername(ADMIN_USERNAME).orElse(null);

        if (admin == null) {
            log.info("Initializing default admin user '{}'...", ADMIN_USERNAME);

            // Create admin user
            admin = new UserEntity();
            admin.setUsername(ADMIN_USERNAME);
            admin.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
            admin.setRealName(ADMIN_REAL_NAME);
            admin.setStatus(1);
            admin = userRepository.save(admin);

            // Assign SUPER_ADMIN role to admin user
            UserRoleEntity userRole = new UserRoleEntity();
            userRole.setUserId(admin.getId());
            userRole.setRoleId(superAdminRole.getId());
            userRoleRepository.save(userRole);

            log.info("Default admin user '{}' created successfully (id={})", ADMIN_USERNAME, admin.getId());
        } else {
            // Re-encode password to ensure it's always BCrypt-encoded
            String encodedPassword = passwordEncoder.encode(ADMIN_PASSWORD);
            if (!passwordEncoder.matches(ADMIN_PASSWORD, admin.getPassword())) {
                log.info("Admin user '{}' exists but password needs re-encoding", ADMIN_USERNAME);
                admin.setPassword(encodedPassword);
                userRepository.save(admin);
            } else {
                log.info("Admin user '{}' already exists with valid password", ADMIN_USERNAME);
            }

            // Ensure admin has the SUPER_ADMIN role
            if (!userRoleRepository.findByUserId(admin.getId()).stream()
                    .anyMatch(ur -> ur.getRoleId().equals(superAdminRole.getId()))) {
                UserRoleEntity userRole = new UserRoleEntity();
                userRole.setUserId(admin.getId());
                userRole.setRoleId(superAdminRole.getId());
                userRoleRepository.save(userRole);
                log.info("Assigned SUPER_ADMIN role to existing admin user");
            }
        }
    }
}
