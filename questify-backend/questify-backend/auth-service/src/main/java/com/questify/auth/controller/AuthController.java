package com.questify.auth.controller;

import com.questify.auth.dto.LoginRequest;
import com.questify.auth.dto.LoginResponse;
import com.questify.auth.dto.UserResponse;
import com.questify.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // Stateless JWT — nothing to invalidate server-side yet. If a token
        // blacklist / refresh-token store is added later, clear it here.
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> currentUser(Authentication authentication) {
        return ResponseEntity.ok(authService.getCurrentUser(authentication.getName()));
    }

    @GetMapping("/users")
    public ResponseEntity<java.util.List<UserResponse>> listUsers(Authentication authentication) {
        return ResponseEntity.ok(authService.listUsers(authentication.getName()));
    }

    @PostMapping("/users")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody com.questify.auth.dto.CreateUserRequest request,
                                                  Authentication authentication) {
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                .body(authService.createUser(request, authentication.getName()));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id,
                                                  @RequestBody com.questify.auth.dto.UpdateUserRequest request,
                                                  Authentication authentication) {
        return ResponseEntity.ok(authService.updateUser(id, request, authentication.getName()));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id, Authentication authentication) {
        authService.deleteUser(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
