package com.simon.user_service.service;

import com.simon.exception.ExistsException;
import com.simon.exception.NotFoundException;
import com.simon.user_service.exception.RentException;
import com.simon.user_service.model.Account;
import com.simon.user_service.model.Order;
import com.simon.user_service.repository.AccountRepository;
import com.simon.user_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public Account findById(int id) {
        return accountRepository.findById(id).orElseThrow(() -> new NotFoundException("Doesn't exists account with id: " + id));
    }

    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    public void add(int id) {
        if (accountRepository.existsById(id)) throw new ExistsException("Already exists account with id: " + id);
        accountRepository.save(new Account(id));
    }

    public void deleteById(int id) {
        List<Order> open_orders = orderRepository.findByAccount_IdAndRentEndIsNull(id);
        if (!open_orders.isEmpty()) throw new ExistsException("Account: " + id + " - have open orders: "
                + open_orders.stream().map(order -> order.getId().toString())
                .collect(Collectors.joining(", ")));
        if (!accountRepository.existsById(id)) throw new NotFoundException("Doesn't exists account with id: " + id);
        accountRepository.deleteById(id);
    }

    @Transactional
    public Account changeBalanceOn(int id, BigDecimal changeOb) {
        Account user = findById(id);
        BigDecimal newBalance = user.getBalanceUSD().add(changeOb);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0)
            throw new RentException("There are insufficient funds on the balance");
        user.setBalanceUSD(newBalance);
        return user;
    }
}
