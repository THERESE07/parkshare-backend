package rw.parkshare.backend.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.parkshare.backend.common.dto.RoleRequestDto;
import rw.parkshare.backend.common.dto.RoleResponseDto;
import rw.parkshare.backend.user.entity.Role;
import rw.parkshare.backend.user.repository.RoleRepository;

import java.util.List;

@Service
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /**
     * Create a new role.
     */
    public RoleResponseDto createRole(RoleRequestDto request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Role request is required."
            );
        }

        if (request.getName() == null
                || request.getName().isBlank()) {
            throw new IllegalArgumentException(
                    "Role name is required."
            );
        }

        if (roleRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException(
                    "A role with this name already exists."
            );
        }

        Role role = new Role();
        role.setName(request.getName());

        Role savedRole = roleRepository.save(role);

        return toResponseDto(savedRole);
    }

    /**
     * Get a role by ID.
     */
    @Transactional(readOnly = true)
    public RoleResponseDto getRoleById(Long id) {

        Role role = roleRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Role not found with ID: " + id
                        )
                );

        return toResponseDto(role);
    }

    /**
     * Get a role by name.
     */
    @Transactional(readOnly = true)
    public RoleResponseDto getRoleByName(String name) {

        Role role = roleRepository.findByName(name)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Role not found with name: " + name
                        )
                );

        return toResponseDto(role);
    }

    /**
     * Get all roles.
     */
    @Transactional(readOnly = true)
    public List<RoleResponseDto> getAllRoles() {

        return roleRepository.findAll()
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    /**
     * Update a role.
     */
    public RoleResponseDto updateRole(
            Long id,
            RoleRequestDto request
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Role request is required."
            );
        }

        if (request.getName() == null
                || request.getName().isBlank()) {
            throw new IllegalArgumentException(
                    "Role name is required."
            );
        }

        Role existingRole = roleRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Role not found with ID: " + id
                        )
                );

        if (!existingRole.getName().equals(request.getName())
                && roleRepository.existsByName(request.getName())) {

            throw new IllegalArgumentException(
                    "A role with this name already exists."
            );
        }

        existingRole.setName(request.getName());

        Role savedRole = roleRepository.save(existingRole);

        return toResponseDto(savedRole);
    }

    /**
     * Check whether a role exists.
     */
    @Transactional(readOnly = true)
    public boolean roleExists(Long id) {
        return roleRepository.existsById(id);
    }

    /**
     * Check whether a role name already exists.
     */
    @Transactional(readOnly = true)
    public boolean roleNameExists(String name) {
        return roleRepository.existsByName(name);
    }

    /**
     * Delete a role.
     */
    public void deleteRole(Long id) {

        Role role = roleRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Role not found with ID: " + id
                        )
                );

        roleRepository.delete(role);
    }

    /**
     * Convert Role entity to response DTO.
     */
    private RoleResponseDto toResponseDto(Role role) {

        return new RoleResponseDto(
                role.getId(),
                role.getName()
        );
    }
}