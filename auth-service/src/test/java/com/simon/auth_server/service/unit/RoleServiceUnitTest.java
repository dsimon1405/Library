package com.simon.auth_server.service.unit;

import com.simon.auth_server.model.Role;
import com.simon.auth_server.repository.RoleRepository;
import com.simon.auth_server.service.RoleService;
import com.simon.exception.ExistsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceUnitTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleService roleService;

    @Test
    void findByNameTransactional_WhenRoleExists_ReturnsRole() {
        Role role = new Role();
        role.setName("ADMIN");
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(role));

        Role result = roleService.findByNameTransactional("ADMIN");

        assertNotNull(result);
        assertEquals("ADMIN", result.getName());
        verify(roleRepository, times(1)).findByName("ADMIN");
    }

    @Test
    void findByNameTransactional_WhenRoleDoesNotExist_ThrowsExistsException() {
        when(roleRepository.findByName("USER")).thenReturn(Optional.empty());

        ExistsException exception =
                assertThrows(ExistsException.class, () -> roleService.findByNameTransactional("USER"));

        assertEquals("Doesn't exists role: USER", exception.getMessage());
        verify(roleRepository, times(1)).findByName("USER");
    }
}
