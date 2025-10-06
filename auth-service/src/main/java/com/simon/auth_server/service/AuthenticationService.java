package com.simon.auth_server.service;

import com.simon.auth_server.model.User;
import com.simon.auth_server.model.dto.UserRequest;
import com.simon.auth_server.jwt.JWTToken;
import com.simon.auth_server.jwt.JWTUtil;
import com.simon.exception.ExistsException;
import com.simon.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final RoleService roleService;
    private final JWTUtil jwtUtil;

    @Transactional      //  transaction to add a role to a user
    public JWTToken registerUser(UserRequest userRequest, String roleName) {
        if (userService.existsByName(userRequest.name())) throw new ExistsException("User already exists");

        User user = UserRequest.toUser(userRequest).addRole(roleService.findByNameTransactional(roleName));
        user.setPassword(passwordEncoder.encode(userRequest.password()));
        user = userService.addUserTransactional(user);

        return jwtUtil.createToken(user);
    }

    public JWTToken loginUser(UserRequest userDTO) {
        User user = userService.findByName(userDTO.name());
        if (!passwordEncoder.matches(userDTO.password(), user.getPassword()))
            throw new NotFoundException("User with requested name or password doesn't exists");
        return jwtUtil.createToken(user);
    }
}
