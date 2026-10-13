package rw.parkshare.backend.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.parkshare.backend.common.dto.UserRequestDto;
import rw.parkshare.backend.common.dto.UserResponseDto;
import rw.parkshare.backend.user.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Create a new user.
     */
    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(
            @RequestBody UserRequestDto request
    ) {
        UserResponseDto createdUser =
                userService.createUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdUser);
    }

    /**
     * Get a user by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                userService.getUserById(id)
        );
    }

    /**
     * Get a user by email.
     */
    @GetMapping("/email")
    public ResponseEntity<UserResponseDto> getUserByEmail(
            @RequestParam String email
    ) {
        return ResponseEntity.ok(
                userService.getUserByEmail(email)
        );
    }

    /**
     * Get all users.
     */
    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        return ResponseEntity.ok(
                userService.getAllUsers()
        );
    }

    /**
     * Update an existing user.
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable Long id,
            @RequestBody UserRequestDto request
    ) {
        return ResponseEntity.ok(
                userService.updateUser(
                        id,
                        request
                )
        );
    }

    /**
     * Update a user's status.
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<UserResponseDto> updateUserStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        return ResponseEntity.ok(
                userService.updateUserStatus(
                        id,
                        status
                )
        );
    }

    /**
     * Check whether a user exists.
     */
    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> userExists(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                userService.userExists(id)
        );
    }

    /**
     * Check whether an email is already registered.
     */
    @GetMapping("/email/exists")
    public ResponseEntity<Boolean> emailExists(
            @RequestParam String email
    ) {
        return ResponseEntity.ok(
                userService.emailExists(email)
        );
    }

    /**
     * Delete a user.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id
    ) {
        userService.deleteUser(id);

        return ResponseEntity.noContent().build();
    }
}