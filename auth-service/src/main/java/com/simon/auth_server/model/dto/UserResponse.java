package com.simon.auth_server.model.dto;

import com.simon.auth_server.model.User;

public record UserResponse(int id, String name) {

    public static UserResponse toUserResponse(User user) {
        return new UserResponse(user.getId(), user.getName());
    }
}
