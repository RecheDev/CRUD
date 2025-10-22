package com.portfolio.usermanagement.service;

import com.portfolio.usermanagement.dto.response.UserResponse;
import com.portfolio.usermanagement.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {

    Page<UserResponse> getAllUsers(Pageable pageable);

    UserResponse getUserById(UUID id);

    UserResponse getCurrentUser(String username);

    UserResponse updateUser(UUID id, User userDetails);

    void deleteUser(UUID id);

    Page<UserResponse> searchUsers(String search, Pageable pageable);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
