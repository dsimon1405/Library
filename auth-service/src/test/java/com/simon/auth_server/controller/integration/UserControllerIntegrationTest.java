package com.simon.auth_server.controller.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simon.auth_server.model.Role;
import com.simon.auth_server.model.User;
import com.simon.auth_server.model.dto.UserRequest;
import com.simon.auth_server.repository.RoleRepository;
import com.simon.auth_server.repository.UserRepository;
import com.simon.name.PathRoles;
import com.simon.name.UserRoles;
import com.simon.utils.HttpRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "/clear-database.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class UserControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired RoleRepository roleRepository;
    static String uri = "/api/v1/user";

    @TestConfiguration
    static class MockConfig {
        @Bean
        @Primary
        HttpRequest mockHttpRequest() {     //  avoid requests to user-service
            return Mockito.mock(HttpRequest.class);
        }
    }

    @BeforeEach
    void setup() {
        Role role = new Role();
        role.setName(UserRoles.ROLE_USER);
        roleRepository.save(role);
    }

    @Test
    void register_ShouldReturnToken() throws Exception {
        UserRequest req = new UserRequest("john", "password");

        mockMvc.perform(post(uri + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void login_WhenUserExists_ShouldReturnToken() throws Exception {
        String pass = "password";
        userRepository.save(new User("john", passwordEncoder.encode(pass)));

        UserRequest req = new UserRequest("john", pass);

        mockMvc.perform(post(uri + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void findAll_ShouldReturnEmptyList() throws Exception {
        mockMvc.perform(get(uri + PathRoles.ADMIN + "/all"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void deleteUserById_WhenUserExists_ShouldReturnOk() throws Exception {
        User user = userRepository.save(new User("john", "pass"));

        mockMvc.perform(delete(uri + PathRoles.ADMIN + "/delete/" + user.getId()))
                .andExpect(status().isOk());
    }
}
