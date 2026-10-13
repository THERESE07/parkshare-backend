package rw.parkshare.backend.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.parkshare.backend.common.dto.RoleRequestDto;
import rw.parkshare.backend.common.dto.RoleResponseDto;
import rw.parkshare.backend.user.service.RoleService;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * Create a new role.
     */
    @PostMapping
    public ResponseEntity<RoleResponseDto> createRole(
            @RequestBody RoleRequestDto request
    ) {
        RoleResponseDto createdRole =
                roleService.createRole(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdRole);
    }

    /**
     * Get a role by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<RoleResponseDto> getRoleById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                roleService.getRoleById(id)
        );
    }

    /**
     * Get a role by name.
     */
    @GetMapping("/name")
    public ResponseEntity<RoleResponseDto> getRoleByName(
            @RequestParam String name
    ) {
        return ResponseEntity.ok(
                roleService.getRoleByName(name)
        );
    }

    /**
     * Get all roles.
     */
    @GetMapping
    public ResponseEntity<List<RoleResponseDto>> getAllRoles() {
        return ResponseEntity.ok(
                roleService.getAllRoles()
        );
    }

    /**
     * Update a role.
     */
    @PutMapping("/{id}")
    public ResponseEntity<RoleResponseDto> updateRole(
            @PathVariable Long id,
            @RequestBody RoleRequestDto request
    ) {
        return ResponseEntity.ok(
                roleService.updateRole(
                        id,
                        request
                )
        );
    }

    /**
     * Check whether a role exists.
     */
    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> roleExists(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                roleService.roleExists(id)
        );
    }

    /**
     * Check whether a role name already exists.
     */
    @GetMapping("/name/exists")
    public ResponseEntity<Boolean> roleNameExists(
            @RequestParam String name
    ) {
        return ResponseEntity.ok(
                roleService.roleNameExists(name)
        );
    }

    /**
     * Delete a role.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(
            @PathVariable Long id
    ) {
        roleService.deleteRole(id);

        return ResponseEntity.noContent().build();
    }
}