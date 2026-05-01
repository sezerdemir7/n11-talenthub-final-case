package com.demir.ecommerce.userservice.service;


import com.demir.ecommerce.userservice.dto.user.request.UpdateUserRequest;
import com.demir.ecommerce.userservice.dto.user.response.UserResponse;

public interface UserService {

    UserResponse getCurrentUser(String email);

    UserResponse getUserById(Long id);

    boolean existsById(Long id);

    UserResponse getInternalUserById(Long id);

    UserResponse updateCurrentUser(String email, UpdateUserRequest request);
}