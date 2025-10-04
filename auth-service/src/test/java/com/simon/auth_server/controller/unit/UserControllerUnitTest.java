package com.simon.auth_server.controller.unit;

import com.simon.auth_server.controller.UserController;
import com.simon.auth_server.model.User;
import com.simon.auth_server.model.dto.UserRequest;
import com.simon.auth_server.model.dto.UserResponse;
import com.simon.auth_server.jwt.JWTToken;
import com.simon.auth_server.service.AuthenticationService;
import com.simon.auth_server.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerUnitTest {

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    void register_ShouldReturnJWTToken() {
        UserRequest request = new UserRequest("john", "123");
        JWTToken token = new JWTToken("Bearer", "mocktoken123");

        when(authenticationService.registerUser(eq(request), anyString())).thenReturn(token);

        ResponseEntity<JWTToken> response = userController.register(request);

        assertEquals(201, response.getStatusCode().value());
        assertEquals("mocktoken123", response.getBody().token());
        verify(authenticationService).registerUser(eq(request), eq("ROLE_USER"));
    }

    @Test
    void login_ShouldReturnJWTToken() {
        UserRequest request = new UserRequest("john", "123");
        JWTToken token = new JWTToken("Bearer", "logintoken456");

        when(authenticationService.loginUser(request)).thenReturn(token);

        ResponseEntity<JWTToken> response = userController.login(request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("logintoken456", response.getBody().token());
        verify(authenticationService).loginUser(request);
    }

    @Test
    void findAll_ShouldReturnListOfUsers() {
        User u1 = new User();
        u1.setId(1);
        User u2 = new User();
        u2.setId(2);
        when(userService.findAll()).thenReturn(List.of(u1, u2));

        List<UserResponse> result = userController.finAll();

        assertEquals(2, result.size());
        verify(userService).findAll();
    }

    @Test
    void deleteUserById_ShouldCallService() {
        userController.deleteUserById(10);

        verify(userService).deleteById(10);
    }
}
