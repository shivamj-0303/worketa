package com.worketa.modules.users.controller;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.worketa.common.response.ApiResponse;
import com.worketa.modules.users.dto.PasswordChangeRequest;
import com.worketa.modules.users.entity.User;
import com.worketa.modules.users.service.UserService;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<User>> create(@RequestBody CreateUserRequest req) {
        User user = service.createUser(req.getFullName(), req.getEmail(), req.getPassword(), req.getRole());
        return ResponseEntity.ok(ApiResponse.ok("User created", user));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> list() {
        List<User> users = service.listByOrg();
        return ResponseEntity.ok(ApiResponse.ok("Users retrieved", users));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getById(@PathVariable UUID id) {
        User user = service.getById(id);
        return ResponseEntity.ok(ApiResponse.ok("User retrieved", user));
    }

    @PostMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changeMyPassword(
            @RequestBody PasswordChangeRequest request,
            Principal principal) {

        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new IllegalStateException("Authenticated user is required");
        }

        UUID userId = UUID.fromString(principal.getName());
        service.changePassword(userId, request.getCurrentPassword(), request.getNewPassword());

        return ResponseEntity.ok(ApiResponse.ok("Password updated successfully", null));
    }

    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changeMyPasswordPut(
            @RequestBody PasswordChangeRequest request,
            Principal principal) {
        return changeMyPassword(request, principal);
    }

    public static class CreateUserRequest {
        private String fullName;
        private String email;
        private String password;
        private String role;

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }
}
