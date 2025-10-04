package com.simon.auth_server.service.unit;

import com.simon.auth_server.model.Role;
import com.simon.auth_server.model.User;
import com.simon.auth_server.repository.UserRepository;
import com.simon.auth_server.service.UserService;
import com.simon.exception.NotFoundException;
import com.simon.name.PathRoles;
import com.simon.name.UserRoles;
import com.simon.utils.HttpRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private HttpRequest httpRequest;
    @InjectMocks
    private UserService userService;

    @Test
    void findAll_ShouldReturnUsers() {
        List<User> users = Arrays.asList(new User("john", "pass"), new User("jane", "pass"));
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.findAll();

        assertEquals(2, result.size());
        verify(userRepository).findAll();
    }

    @Test
    void findByName_WhenExists_ShouldReturnUser() {
        User user = new User("john", "pass");
        when(userRepository.findByName("john")).thenReturn(Optional.of(user));

        User result = userService.findByName("john");

        assertEquals("john", result.getName());
    }

    @Test
    void findByName_WhenNotExists_ShouldThrow() {
        when(userRepository.findByName("unknown")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.findByName("unknown"));
    }

    @Test
    void addUserTransactional_ShouldSaveAndCallHttpRequest() {
        User user = new User("john", "pass");
        user.setId(1);
        when(userRepository.save(user)).thenReturn(user);

        userService.addUserTransactional(user);

        verify(userRepository).save(user);

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HttpMethod> methodCaptor = ArgumentCaptor.forClass(HttpMethod.class);
        ArgumentCaptor<Map<String, Integer>> idCaptor = ArgumentCaptor.forClass(Map.class);

        verify(httpRequest).request(
                urlCaptor.capture(),
                methodCaptor.capture(),
                isNull(),
                eq(Void.class),
                idCaptor.capture()
        );

        assertEquals(UserService.serviceURL + "/user-service/api/v1/account" + PathRoles.SERVICE
                + "/add/{id}", urlCaptor.getValue());
        assertEquals(HttpMethod.POST, methodCaptor.getValue());
        assertEquals(1, idCaptor.getValue().get("id"));
    }

    @Test
    void deleteById_WhenUserExists_ShouldCallHttpRequestAndDelete() {
        User user = new User();
        Role role = new Role();
        role.setName(UserRoles.ROLE_USER);
        user.setRoles(Set.of(role));
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        userService.deleteById(1);

        verify(httpRequest).request(
                anyString(),
                eq(HttpMethod.DELETE),
                isNull(),
                eq(Void.class),
                eq(Map.of("id", 1))
        );

        verify(userRepository).deleteById(1);
        verify(userRepository).findById(1);
    }

    @Test
    void deleteById_WhenUserNotExists_ShouldThrow() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.deleteById(1));
    }
}
