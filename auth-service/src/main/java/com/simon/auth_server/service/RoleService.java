package com.simon.auth_server.service;

import com.simon.auth_server.model.Role;
import com.simon.auth_server.repository.RoleRepository;
import com.simon.exception.ExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    @Transactional
    public Role findByNameTransactional(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new ExistsException("Doesn't exists role: " + name)); //  roleName roles must equals roles in table library_auth_db
    }
}
