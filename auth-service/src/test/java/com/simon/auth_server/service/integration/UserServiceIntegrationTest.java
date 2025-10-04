package com.simon.auth_server.service.integration;

import com.simon.auth_server.model.User;
import com.simon.auth_server.repository.UserRepository;
import com.simon.auth_server.service.UserService;
import com.simon.exception.NotFoundException;
import com.simon.utils.HttpRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
public class UserServiceIntegrationTest {

    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepository;

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
        userRepository.deleteAll();
    }

    @Test
    void findByName_UserExists_ReturnUser() {
        User saved = userRepository.save(new User("name", "pass"));

        User found = userService.findByName("name");

        assertThat(found).isNotNull();
        assertThat(saved.getId()).isEqualTo(found.getId());
    }

    @Test
    void findByName_UserNotExists_Throw() {
        assertThatThrownBy(() -> userService.findByName("name"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deleteById_UserExists_Delete() {
        User saved = userRepository.save(new User("name", "pass"));

        userService.deleteById(saved.getId());

        assertThat(userRepository.existsById(saved.getId())).isFalse();
    }

    @Test
    void deleteById_UserNotExists_Throw() {
        assertThatThrownBy(() -> userService.deleteById(666))
                .isInstanceOf(NotFoundException.class);
    }
}
