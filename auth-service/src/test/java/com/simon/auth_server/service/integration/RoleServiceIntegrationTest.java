package com.simon.auth_server.service.integration;

import com.simon.auth_server.model.Role;
import com.simon.auth_server.repository.RoleRepository;
import com.simon.auth_server.service.RoleService;
import com.simon.exception.ExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RoleServiceIntegrationTest {

    @Autowired
    private RoleService roleService;

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    void init() {
        roleRepository.deleteAll();
    }

    @Test
    void findByNameTransactional_WhenRoleExists_ShouldReturnRole() {
        Role saved = new Role();
        saved.setName("ADMIN");
        roleRepository.save(saved);

        Role found = roleService.findByNameTransactional("ADMIN");

        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("ADMIN");
    }

    @Test
    void findByNameTransactional_WhenRoleNotExists_ShouldThrowExistsException() {
        ExistsException thrown = assertThrows(ExistsException.class, () -> roleService.findByNameTransactional("UNKNOWN"));

        assertThat(thrown).hasMessageContaining("Doesn't exists role: UNKNOWN");
    }
}
