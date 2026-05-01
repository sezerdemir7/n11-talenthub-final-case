package com.demir.ecommerce.userservice.controller;

import com.demir.ecommerce.commonlib.dto.RestResponse;
import com.demir.ecommerce.userservice.dto.user.request.UpdateUserRequest;
import com.demir.ecommerce.userservice.dto.user.response.UserResponse;
import com.demir.ecommerce.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "User management operations")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Get current user", description = "Returns authenticated user information")
    @GetMapping("/me")
    public ResponseEntity<RestResponse<UserResponse>> getCurrentUser(Authentication authentication) {
        UserResponse response = userService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(RestResponse.of(response));
    }

    @Operation(summary = "Update current user", description = "Updates authenticated user's profile")
    @PutMapping("/me")
    public ResponseEntity<RestResponse<UserResponse>> updateCurrentUser(
            Authentication authentication,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        UserResponse response = userService.updateCurrentUser(authentication.getName(), request);
        return ResponseEntity.ok(RestResponse.of(response, "User updated successfully"));
    }

    @Operation(summary = "Get user by id", description = "Returns user information by id")
    @GetMapping("/{id}")
    public ResponseEntity<RestResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(RestResponse.of(response));
    }

    @GetMapping("/{id}/exists")
    public boolean existsById(@PathVariable Long id) {
        return userService.existsById(id);
    }

    @GetMapping("/internal/{id}")
    public UserResponse getInternalUserById(@PathVariable Long id) {
        return userService.getInternalUserById(id);
    }
}