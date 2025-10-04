package com.simon.auth_server.service.integration;

import com.simon.auth_server.model.Role;
import com.simon.auth_server.model.User;
import com.simon.auth_server.model.dto.UserRequest;
import com.simon.auth_server.repository.RoleRepository;
import com.simon.auth_server.repository.UserRepository;
import com.simon.auth_server.jwt.JWTToken;
import com.simon.auth_server.service.AuthenticationService;
import com.simon.exception.ExistsException;
import com.simon.exception.NotFoundException;
import com.simon.name.PathRoles;
import static org.assertj.core.api.Assertions.*;

import com.simon.utils.HttpRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "/clear-database.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class AuthenticationServiceIntegrationTest {

    @Autowired
    AuthenticationService authenticationService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    @TestConfiguration
    static class MockConfig {
        @Primary
        @Bean
        HttpRequest mockHttpRequest() {
            return Mockito.mock(HttpRequest.class);
        }
    }

    @BeforeEach
    void beforeEach() {
        Role role = new Role();
        role.setName(PathRoles.USER);
        roleRepository.save(role);
    }

    @Test
    void registerUser_ParamsValid_ReturnJwtToken() {
        UserRequest userRequest = new UserRequest("name", "pass");

        JWTToken token = authenticationService.registerUser(userRequest, PathRoles.USER);

        assertThat(token).isNotNull();
        assertThat(token.token()).isNotEmpty();
    }

    @Test
    void registerUser_UserExists_Throws() {
        userRepository.save(new User("name", "pass"));

        assertThatThrownBy(() ->
                authenticationService.registerUser(new UserRequest("name", "pass"), PathRoles.USER))
                .isInstanceOf(ExistsException.class);
    }

    @Test
    void loginUser_UserExists_ReturnToken() {
        userRepository.save(new User("name", passwordEncoder.encode("pass")));

        JWTToken token = authenticationService.loginUser(new UserRequest("name", "pass"));

        assertThat(token).isNotNull();
        assertThat(token.token()).isNotEmpty();
    }

    @Test
    void loginUser_WrongPassword_Throws() {
        userRepository.save(new User("name", passwordEncoder.encode("pass1")));

        assertThatThrownBy(() ->
                authenticationService.loginUser(new UserRequest("name", "pass2")))
                .isInstanceOf(NotFoundException.class);
    }
}
