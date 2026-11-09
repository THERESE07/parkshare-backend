package rw.parkshare.backend.user.service;

import rw.parkshare.backend.common.dto.PageResponse;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.parkshare.backend.common.dto.UserRequestDto;
import rw.parkshare.backend.common.dto.UserResponseDto;
import rw.parkshare.backend.user.entity.User;
import rw.parkshare.backend.user.repository.UserRepository;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * Create a new user.
     */
    public UserResponseDto createUser(UserRequestDto request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "User request is required."
            );
        }

        if (request.getEmail() == null
                || request.getEmail().isBlank()) {
            throw new IllegalArgumentException(
                    "Email is required."
            );
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(
                    "A user with this email already exists."
            );
        }

        if (request.getPassword() == null
                || request.getPassword().isBlank()) {
            throw new IllegalArgumentException(
                    "Password is required."
            );
        }

        User user = new User();

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        user.setPasswordHash(
                passwordEncoder.encode(request.getPassword())
        );

        if (request.getStatus() == null
                || request.getStatus().isBlank()) {
            user.setStatus("ACTIVE");
        } else {
            user.setStatus(request.getStatus());
        }

        OffsetDateTime now = OffsetDateTime.now();

        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        User savedUser = userRepository.save(user);

        return toResponseDto(savedUser);
    }

    /**
     * Get a user by ID.
     */
    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found with ID: " + id
                        )
                );

        return toResponseDto(user);
    }

    /**
     * Get a user by email.
     */
    @Transactional(readOnly = true)
    public UserResponseDto getUserByEmail(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found with email: " + email
                        )
                );

        return toResponseDto(user);
    }

    /**
     * Get all users.
     */
    @Transactional(readOnly = true)
    public PageResponse<UserResponseDto> getAllUsers(int page, int size) {
        return PageResponse.of(
                userRepository.findAll(PageResponse.request(page, size, "createdAt")),
                this::toResponseDto
        );
    }

    /**
     * Update an existing user.
     */
    public UserResponseDto updateUser(
            Long id,
            UserRequestDto request
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "User request is required."
            );
        }

        User existingUser = userRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found with ID: " + id
                        )
                );

        if (request.getEmail() != null
                && !request.getEmail().equals(
                existingUser.getEmail()
        )
                && userRepository.existsByEmail(
                request.getEmail()
        )) {
            throw new IllegalArgumentException(
                    "A user with this email already exists."
            );
        }

        if (request.getFirstName() != null) {
            existingUser.setFirstName(
                    request.getFirstName()
            );
        }

        if (request.getLastName() != null) {
            existingUser.setLastName(
                    request.getLastName()
            );
        }

        if (request.getEmail() != null) {
            existingUser.setEmail(
                    request.getEmail()
            );
        }

        if (request.getPhone() != null) {
            existingUser.setPhone(
                    request.getPhone()
            );
        }

        if (request.getPassword() != null
                && !request.getPassword().isBlank()) {

            existingUser.setPasswordHash(
                    passwordEncoder.encode(
                            request.getPassword()
                    )
            );
        }

        if (request.getStatus() != null
                && !request.getStatus().isBlank()) {

            existingUser.setStatus(
                    request.getStatus()
            );
        }

        existingUser.setUpdatedAt(
                OffsetDateTime.now()
        );

        User savedUser = userRepository.save(
                existingUser
        );

        return toResponseDto(savedUser);
    }

    /**
     * Change the user's status.
     */
    public UserResponseDto updateUserStatus(
            Long id,
            String status
    ) {

        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException(
                    "Status is required."
            );
        }

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found with ID: " + id
                        )
                );

        user.setStatus(status);
        user.setUpdatedAt(
                OffsetDateTime.now()
        );

        User savedUser = userRepository.save(user);

        return toResponseDto(savedUser);
    }

    /**
     * Check whether a user exists.
     */
    @Transactional(readOnly = true)
    public boolean userExists(Long id) {
        return userRepository.existsById(id);
    }

    /**
     * Check whether an email is already registered.
     */
    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Delete a user.
     */
    public void deleteUser(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found with ID: " + id
                        )
                );

        userRepository.delete(user);
    }

    /**
     * Convert User entity to safe response DTO.
     *
     * passwordHash is intentionally excluded.
     */
    private UserResponseDto toResponseDto(User user) {

        return new UserResponseDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}