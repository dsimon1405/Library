package com.simon.auth_server.service.unit;

import com.simon.auth_server.model.Role;
import com.simon.auth_server.model.User;
import com.simon.auth_server.model.dto.UserRequest;
import com.simon.auth_server.jwt.JWTToken;
import com.simon.auth_server.jwt.JWTUtil;
import com.simon.auth_server.service.AuthenticationService;
import com.simon.auth_server.service.RoleService;
import com.simon.auth_server.service.UserService;
import com.simon.exception.ExistsException;
import com.simon.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceUnitTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserService userService;

    @Mock
    private RoleService roleService;

    @Mock
    private JWTUtil jwtUtil;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void registerUser_WhenUserDoesNotExist_ShouldRegisterAndReturnToken() {
        UserRequest request = new UserRequest("john", "123");
        User user = new User("john", "encoded123");
        JWTToken token = new JWTToken("Bearer", "mocktoken");

        Role mockRole = new Role();
        mockRole.setName("ROLE_USER");

        when(userService.existsByName("john")).thenReturn(false);
        when(roleService.findByNameTransactional("ROLE_USER")).thenReturn(mockRole);
        when(passwordEncoder.encode("123")).thenReturn("encoded123");
        when(userService.addUserTransactional(any(User.class))).thenReturn(user);
        when(jwtUtil.createToken(user)).thenReturn(token);

        JWTToken result = authenticationService.registerUser(request, "ROLE_USER");

        assertEquals("mocktoken", result.token());
        verify(userService).addUserTransactional(any(User.class));
        verify(jwtUtil).createToken(user);
    }


    @Test
    void registerUser_WhenUserExists_ShouldThrowException() {
        UserRequest request = new UserRequest("john", "123");

        when(userService.existsByName("john")).thenReturn(true);

        assertThrows(ExistsException.class,
                () -> authenticationService.registerUser(request, "ROLE_USER"));

        verify(userService, never()).addUserTransactional(any());
    }

    @Test
    void loginUser_WhenCredentialsAreValid_ShouldReturnToken() {
        UserRequest request = new UserRequest("john", "123");
        User user = new User("john", "encoded123");
        JWTToken token = new JWTToken("Bearer", "logintoken");

        when(userService.findByName("john")).thenReturn(user);
        when(passwordEncoder.matches("123", "encoded123")).thenReturn(true);
        when(jwtUtil.createToken(user)).thenReturn(token);

        JWTToken result = authenticationService.loginUser(request);

        assertEquals("logintoken", result.token());
        verify(jwtUtil).createToken(user);
    }

    @Test
    void loginUser_WhenPasswordInvalid_ShouldThrowException() {
        UserRequest request = new UserRequest("john", "wrong");
        User user = new User("john", "encoded123");

        when(userService.findByName("john")).thenReturn(user);
        when(passwordEncoder.matches("wrong", "encoded123")).thenReturn(false);

        assertThrows(NotFoundException.class,
                () -> authenticationService.loginUser(request));

        verify(jwtUtil, never()).createToken(any());
    }
}
