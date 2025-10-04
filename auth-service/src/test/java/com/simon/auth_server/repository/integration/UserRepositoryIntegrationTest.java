package com.simon.auth_server.repository.integration;

import com.simon.auth_server.model.Role;
import com.simon.auth_server.model.User;
import com.simon.auth_server.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Sql(scripts = "/clear-database.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class UserRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    private User createUserWithRoles() {
        Role roleUser = new Role();
        roleUser.setName("ROLE_USER");

        User user = new User();
        user.setName("john_doe");
        user.setPassword("password123");
        user.setRoles(Set.of(roleUser));

        return userRepository.save(user);
    }

    @Test
    void testExistsByName() {
        createUserWithRoles();

        boolean exists = userRepository.existsByName("john_doe");

        assertTrue(exists);
        assertFalse(userRepository.existsByName("jane_doe"));
    }

    @Test
    void testFindByName() {
        User savedUser = createUserWithRoles();

        Optional<User> found = userRepository.findByName("john_doe");

        assertThat(found).isPresent();
        assertEquals(savedUser.getId(), found.get().getId());
        assertEquals("john_doe", found.get().getName());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void testFindByIdWithRoles() {
        User savedUser = createUserWithRoles();

        Optional<User> found = userRepository.findById(savedUser.getId());

        assertThat(found).isPresent();
        User user = found.get();

        assertThat(user.getRoles()).hasSize(1);
        assertThat(user.getRoles().iterator().next().getName()).isEqualTo("ROLE_USER");
    }

    @Test
    void testFindById_userNotFound() {
        Optional<User> user = userRepository.findById(9999);

        assertThat(user).isNotPresent();
    }
}
