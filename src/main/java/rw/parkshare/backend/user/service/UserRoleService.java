package rw.parkshare.backend.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.parkshare.backend.common.dto.UserRoleResponseDto;
import rw.parkshare.backend.user.entity.UserRole;
import rw.parkshare.backend.user.entity.UserRoleId;
import rw.parkshare.backend.user.repository.UserRoleRepository;

import java.util.List;

@Service
@Transactional
public class UserRoleService {

    private final UserRoleRepository userRoleRepository;

    public UserRoleService(UserRoleRepository userRoleRepository) {
        this.userRoleRepository = userRoleRepository;
    }

    /**
     * Assign a role to a user.
     */
    public UserRoleResponseDto assignRole(
            Long userId,
            Long roleId
    ) {

        if (userId == null) {
            throw new IllegalArgumentException(
                    "User ID is required."
            );
        }

        if (roleId == null) {
            throw new IllegalArgumentException(
                    "Role ID is required."
            );
        }

        if (userRoleRepository.existsByUserIdAndRoleId(
                userId,
                roleId
        )) {
            throw new IllegalArgumentException(
                    "This role is already assigned to the user."
            );
        }

        UserRole userRole =
                new UserRole(userId, roleId);

        UserRole savedUserRole =
                userRoleRepository.save(userRole);

        return toResponseDto(savedUserRole);
    }

    /**
     * Get all roles assigned to a user.
     */
    @Transactional(readOnly = true)
    public List<UserRoleResponseDto> getRolesByUserId(
            Long userId
    ) {

        return userRoleRepository.findByUserId(userId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    /**
     * Get all users assigned to a role.
     */
    @Transactional(readOnly = true)
    public List<UserRoleResponseDto> getUsersByRoleId(
            Long roleId
    ) {

        return userRoleRepository.findByRoleId(roleId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    /**
     * Check whether a user has a specific role.
     */
    @Transactional(readOnly = true)
    public boolean userHasRole(
            Long userId,
            Long roleId
    ) {
        return userRoleRepository.existsByUserIdAndRoleId(
                userId,
                roleId
        );
    }

    /**
     * Remove a role from a user.
     */
    public void removeRole(
            Long userId,
            Long roleId
    ) {

        UserRoleId userRoleId =
                new UserRoleId(userId, roleId);

        if (!userRoleRepository.existsById(userRoleId)) {
            throw new IllegalArgumentException(
                    "This role assignment does not exist."
            );
        }

        userRoleRepository.deleteById(userRoleId);
    }

    /**
     * Get a specific user-role assignment.
     */
    @Transactional(readOnly = true)
    public UserRoleResponseDto getUserRole(
            Long userId,
            Long roleId
    ) {

        UserRoleId userRoleId =
                new UserRoleId(userId, roleId);

        UserRole userRole =
                userRoleRepository.findById(userRoleId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "User-role assignment not found."
                                )
                        );

        return toResponseDto(userRole);
    }

    /**
     * Convert UserRole entity to response DTO.
     */
    private UserRoleResponseDto toResponseDto(
            UserRole userRole
    ) {

        return new UserRoleResponseDto(
                userRole.getUserId(),
                userRole.getRoleId()
        );
    }
}