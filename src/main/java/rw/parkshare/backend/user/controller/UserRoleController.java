package rw.parkshare.backend.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.parkshare.backend.common.dto.UserRoleResponseDto;
import rw.parkshare.backend.user.service.UserRoleService;

import java.util.List;

@RestController
@RequestMapping("/api/user-roles")
public class UserRoleController {

    private final UserRoleService userRoleService;

    public UserRoleController(UserRoleService userRoleService) {
        this.userRoleService = userRoleService;
    }

    /**
     * Assign a role to a user.
     */
    @PostMapping
    public ResponseEntity<UserRoleResponseDto> assignRole(
            @RequestParam Long userId,
            @RequestParam Long roleId
    ) {
        UserRoleResponseDto userRole =
                userRoleService.assignRole(
                        userId,
                        roleId
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userRole);
    }

    /**
     * Get all roles assigned to a user.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserRoleResponseDto>> getRolesByUserId(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                userRoleService.getRolesByUserId(userId)
        );
    }

    /**
     * Get all users assigned to a role.
     */
    @GetMapping("/role/{roleId}")
    public ResponseEntity<List<UserRoleResponseDto>> getUsersByRoleId(
            @PathVariable Long roleId
    ) {
        return ResponseEntity.ok(
                userRoleService.getUsersByRoleId(roleId)
        );
    }

    /**
     * Check whether a user has a specific role.
     */
    @GetMapping("/check")
    public ResponseEntity<Boolean> userHasRole(
            @RequestParam Long userId,
            @RequestParam Long roleId
    ) {
        return ResponseEntity.ok(
                userRoleService.userHasRole(
                        userId,
                        roleId
                )
        );
    }

    /**
     * Get a specific user-role assignment.
     */
    @GetMapping("/user/{userId}/role/{roleId}")
    public ResponseEntity<UserRoleResponseDto> getUserRole(
            @PathVariable Long userId,
            @PathVariable Long roleId
    ) {
        return ResponseEntity.ok(
                userRoleService.getUserRole(
                        userId,
                        roleId
                )
        );
    }

    /**
     * Remove a role from a user.
     */
    @DeleteMapping("/user/{userId}/role/{roleId}")
    public ResponseEntity<Void> removeRole(
            @PathVariable Long userId,
            @PathVariable Long roleId
    ) {
        userRoleService.removeRole(
                userId,
                roleId
        );

        return ResponseEntity.noContent().build();
    }
}