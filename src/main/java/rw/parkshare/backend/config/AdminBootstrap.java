package rw.parkshare.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import rw.parkshare.backend.common.RwandaPhone;
import rw.parkshare.backend.user.entity.Role;
import rw.parkshare.backend.user.entity.User;
import rw.parkshare.backend.user.entity.UserRole;
import rw.parkshare.backend.user.repository.RoleRepository;
import rw.parkshare.backend.user.repository.UserRepository;
import rw.parkshare.backend.user.repository.UserRoleRepository;

import java.time.OffsetDateTime;

/**
 * Creates the first administrator from PARKSHARE_SECURITY_ADMINEMAIL and
 * PARKSHARE_SECURITY_ADMINPASSWORD when no user with that email exists.
 * Nothing happens if those variables are not set.
 */
@Component
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final ParkShareProperties properties;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminBootstrap(
            ParkShareProperties properties,
            UserRepository userRepository,
            RoleRepository roleRepository,
            UserRoleRepository userRoleRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.properties = properties;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        ParkShareProperties.Security security = properties.getSecurity();
        String email = security.getAdminEmail();
        String password = security.getAdminPassword();
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            return;
        }
        email = email.trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            return;
        }
        if (password.length() < 12) {
            log.warn("Admin bootstrap skipped: PARKSHARE_SECURITY_ADMINPASSWORD must be at least 12 characters");
            return;
        }

        User admin = new User();
        admin.setFirstName("ParkShare");
        admin.setLastName("Admin");
        admin.setEmail(email);
        admin.setPhone(RwandaPhone.requireValid(security.getAdminPhone()));
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setStatus("ACTIVE");
        admin.setCreatedAt(OffsetDateTime.now());
        admin.setUpdatedAt(OffsetDateTime.now());
        admin = userRepository.save(admin);

        Role role = roleRepository.findByName("ADMIN").orElseGet(() -> {
            Role created = new Role();
            created.setName("ADMIN");
            return roleRepository.save(created);
        });
        userRoleRepository.save(new UserRole(admin.getId(), role.getId()));
        log.info("Created bootstrap administrator {}", email);
    }
}
