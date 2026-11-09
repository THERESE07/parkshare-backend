package rw.parkshare.backend.auth;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import rw.parkshare.backend.security.JwtService;
import rw.parkshare.backend.user.entity.Role;
import rw.parkshare.backend.user.entity.User;
import rw.parkshare.backend.user.entity.UserRole;
import rw.parkshare.backend.user.repository.RoleRepository;
import rw.parkshare.backend.user.repository.UserRepository;
import rw.parkshare.backend.user.repository.UserRoleRepository;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final rw.parkshare.backend.auth.otp.AccountVerificationService verificationService;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            UserRoleRepository userRoleRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            rw.parkshare.backend.auth.otp.AccountVerificationService verificationService
    ) {
        this.verificationService = verificationService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @org.springframework.transaction.annotation.Transactional
    public RegisterResponseDto register(RegisterRequestDto request) {

        validateRegistrationRequest(request);

        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw rw.parkshare.backend.common.exception.ApiException.conflict("Email is already registered");
        }

        String phone = rw.parkshare.backend.common.RwandaPhone.requireValid(request.getPhone());
        if (userRepository.existsByPhone(phone)) {
            throw rw.parkshare.backend.common.exception.ApiException.conflict("Phone number is already registered");
        }

        String roleName = request.getAccountType() == null || request.getAccountType().isBlank()
                ? "DRIVER"
                : request.getAccountType().trim().toUpperCase();
        if (!roleName.equals("DRIVER") && !roleName.equals("OPERATOR")) {
            throw new IllegalArgumentException("accountType must be DRIVER or OPERATOR");
        }

        User user = new User();
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPhone(phone);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setStatus("ACTIVE");
        user.setCreatedAt(OffsetDateTime.now());
        user.setUpdatedAt(OffsetDateTime.now());

        User savedUser = userRepository.save(user);

        Role customerRole = roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(roleName);
                    return roleRepository.save(role);
                });

        UserRole userRole = new UserRole(
                savedUser.getId(),
                customerRole.getId()
        );

        userRoleRepository.save(userRole);

        verificationService.sendWelcomeCodes(savedUser);

        return new RegisterResponseDto(
                savedUser.getId(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getEmail(),
                savedUser.getPhone(),
                savedUser.getStatus(),
                customerRole.getName()
        );
    }

    public LoginResponseDto login(LoginRequestDto request) {

        if (request == null
                || request.getEmail() == null
                || request.getEmail().isBlank()
                || request.getPassword() == null
                || request.getPassword().isBlank()) {

            throw new BadCredentialsException("Invalid email or password");
        }

        String email = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new BadCredentialsException("Invalid email or password")
                );

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new BadCredentialsException("User account is not active");
        }

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash()
        )) {
            throw new BadCredentialsException("Invalid email or password");
        }

        List<String> roles = userRoleRepository
                .findByUserId(user.getId())
                .stream()
                .map(UserRole::getRoleId)
                .map(roleRepository::findById)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .map(Role::getName)
                .map(String::toUpperCase)
                .toList();

        String token = jwtService.generateToken(
                user.getEmail(),
                user.getId(),
                roles
        );

        return new LoginResponseDto(
                token,
                "Bearer",
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                roles
        );
    }

    private void validateRegistrationRequest(RegisterRequestDto request) {

        if (request == null) {
            throw new IllegalArgumentException("Registration request is required");
        }

        if (request.getFirstName() == null || request.getFirstName().isBlank()) {
            throw new IllegalArgumentException("First name is required");
        }

        if (request.getLastName() == null || request.getLastName().isBlank()) {
            throw new IllegalArgumentException("Last name is required");
        }

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (request.getPhone() == null || request.getPhone().isBlank()) {
            throw new IllegalArgumentException("Phone is required");
        }

        if (request.getPassword() == null || request.getPassword().length() < 8) {
            throw new IllegalArgumentException(
                    "Password must contain at least 8 characters"
            );
        }

        if (!request.getPassword().matches(".*[A-Za-z].*") || !request.getPassword().matches(".*\\d.*")) {
            throw new IllegalArgumentException(
                    "Password must contain at least one letter and one number"
            );
        }

        if (!request.getEmail().trim().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("Email address is not valid");
        }
    }
}
