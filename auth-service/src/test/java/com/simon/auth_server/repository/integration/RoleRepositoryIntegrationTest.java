package com.simon.auth_server.repository.integration;

import com.simon.auth_server.model.Role;
import com.simon.auth_server.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "/clear-database.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class RoleRepositoryIntegrationTest {

    @Autowired
    RoleRepository roleRepository;

    @Test
    void saveAndFindById() {
        Role role = new Role();
        role.setName("ROLE_USER");

        Role saved = roleRepository.save(role);

        Optional<Role> found = roleRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("ROLE_USER", found.get().getName());
    }

    @Test
    void findByName_existingRole() {
        Role role = new Role();
        role.setName("ROLE_ADMIN");

        roleRepository.save(role);

        Optional<Role> found = roleRepository.findByName("ROLE_ADMIN");

        assertThat(found).isPresent();
        assertEquals("ROLE_ADMIN", found.get().getName());
    }
}
