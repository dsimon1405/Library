package com.simon.auth_server.controller;

import com.simon.auth_server.model.dto.UserRequest;
import com.simon.auth_server.model.dto.UserResponse;
import com.simon.auth_server.jwt.JWTToken;
import com.simon.auth_server.service.AuthenticationService;
import com.simon.auth_server.service.UserService;
import com.simon.name.PathRoles;
import com.simon.name.UserRoles;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final AuthenticationService securityService;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<JWTToken> register(@Valid @RequestBody UserRequest userDTO) {
        return new ResponseEntity<>(securityService.registerUser(userDTO, UserRoles.ROLE_USER), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<JWTToken> login(@Valid @RequestBody UserRequest userDTO) {
        return ResponseEntity.ok().body(securityService.loginUser(userDTO));
    }

    @GetMapping(PathRoles.ADMIN + "/all")
    public List<UserResponse> finAll() {
        return userService.findAll().stream().map(UserResponse::toUserResponse).toList();
    }

    @DeleteMapping(PathRoles.ADMIN + "/delete/{id}")
    public void deleteUserById(@PathVariable int id) {
        userService.deleteById(id);
    }
}
