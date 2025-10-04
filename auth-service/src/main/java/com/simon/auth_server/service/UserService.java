package com.simon.auth_server.service;

import com.simon.auth_server.model.User;
import com.simon.auth_server.repository.UserRepository;
import com.simon.dto.user.OrderDTO;
import com.simon.exception.ExistsException;
import com.simon.exception.NotFoundException;
import com.simon.name.PathRoles;
import com.simon.name.UserRoles;
import com.simon.utils.HttpRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final HttpRequest httpRequest;
    public static final String serviceURL = "http://user-service";    //  using eureka
//    public static final String serviceURL = "http://localhost:8082";

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findByName(String name) {
        return userRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException("User with requested name or password don't exists"));
    }

    public boolean existsByName(String name) {
        return userRepository.existsByName(name);
    }

    @Transactional
    public User addUserTransactional(User user) {
        user = userRepository.save(user);
        httpRequest.request(
                serviceURL + "/user-service/api/v1/account" + PathRoles.SERVICE + "/add/{id}",
                HttpMethod.POST,
                null,
                Void.class,
                Map.of("id", user.getId()));

        return user;
    }

    public User findByIdWithRoles(int id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Doesn't exists user with id: " + id));
    }

    public void deleteById(int id) {
        User user = findByIdWithRoles(id);
        if (user.getRoles().stream().anyMatch(r -> r.getName().equals(UserRoles.ROLE_ADMIN)))
            throw new ExistsException("Can't delete user with ADMIN_ROLE");

        httpRequest.request(
                serviceURL + "/user-service/api/v1/account" + PathRoles.SERVICE + "/delete/{id}",
                HttpMethod.DELETE,
                null,
                Void.class,
                Map.of("id", id));

        userRepository.deleteById(id);
    }
}
//ResponseEntity<List<OrderDTO>> open_orders = httpRequest.request(
//        serviceURL + "/user-service/api/v1/account" + PathRoles.SERVICE + "/delete/{id}",
//        HttpMethod.DELETE,
//        null,
//        new ParameterizedTypeReference<List<OrderDTO>>(){},
//        Map.of("id", id));
//        if (open_orders.getBody() != null)
//        throw new ExistsException("User have open orders: " + open_orders.getBody());
