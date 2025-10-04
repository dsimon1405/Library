package com.simon.auth_server.model.dto;

import com.simon.auth_server.model.User;
import jakarta.validation.constraints.NotEmpty;

public record UserRequest(
        @NotEmpty(message = "name can't be empty") String name,
        @NotEmpty(message = "password can't be empty") String password) {

    public static User toUser(UserRequest dto) {
        return new User(dto.name, dto.password);
    }
}
