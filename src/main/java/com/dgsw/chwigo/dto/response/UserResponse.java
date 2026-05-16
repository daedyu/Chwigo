package com.dgsw.chwigo.dto.response;

import com.dgsw.chwigo.domain.entity.User;
import com.dgsw.chwigo.domain.enums.UserRole;

import java.time.LocalDateTime;

public record UserResponse(Long id, String email, String nickname, String address,
                           UserRole role, LocalDateTime createdAt) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getNickname(),
                user.getAddress(), user.getRole(), user.getCreatedAt());
    }
}
