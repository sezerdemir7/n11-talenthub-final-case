package com.demir.ecommerce.userservice.service.impl;

import com.demir.ecommerce.commonlib.excepption.BusinessException;
import com.demir.ecommerce.userservice.dto.user.request.UpdateUserRequest;
import com.demir.ecommerce.userservice.dto.user.response.UserResponse;
import com.demir.ecommerce.userservice.entity.User;
import com.demir.ecommerce.userservice.exception.message.UserErrorMessage;
import com.demir.ecommerce.userservice.repository.UserRepository;
import com.demir.ecommerce.userservice.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {
        User user = findUserByEmail(email);
        return mapToUserResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = findUserById(id);
        return mapToUserResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getInternalUserById(Long id) {
        User user = findUserById(id);
        return mapToUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateCurrentUser(String email, UpdateUserRequest request) {
        User user = findUserByEmail(email);

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());

        User updatedUser = userRepository.save(user);

        return mapToUserResponse(updatedUser);
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(UserErrorMessage.USER_NOT_FOUND));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(UserErrorMessage.USER_NOT_FOUND));
    }

    private UserResponse mapToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                toRoleNames(user),
                Boolean.TRUE.equals(user.getIsActive())
        );
    }

    private List<String> toRoleNames(User user) {
        return user.getRoles()
                .stream()
                .map(Enum::name)
                .toList();
    }
}
