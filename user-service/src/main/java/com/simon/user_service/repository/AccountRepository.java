package com.simon.user_service.repository;

import com.simon.user_service.model.Account;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AccountRepository extends CrudRepository<Account, Integer> {

    List<Account> findAll();
}
