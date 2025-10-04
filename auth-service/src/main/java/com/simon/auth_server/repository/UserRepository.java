package com.simon.auth_server.repository;

import com.simon.auth_server.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    boolean existsByName(String name);

    Optional<User> findByName(String name);

    @EntityGraph(attributePaths = "roles")
    Optional<User> findById(int id);
}
