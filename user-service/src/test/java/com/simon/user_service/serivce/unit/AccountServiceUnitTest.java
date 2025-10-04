package com.simon.user_service.serivce.unit;

import com.simon.exception.ExistsException;
import com.simon.exception.NotFoundException;
import com.simon.user_service.exception.RentException;
import com.simon.user_service.model.Account;
import com.simon.user_service.model.Order;
import com.simon.user_service.repository.AccountRepository;
import com.simon.user_service.repository.OrderRepository;
import com.simon.user_service.service.AccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceUnitTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    OrderRepository orderRepository;

    @InjectMocks
    private AccountService accountService;


    @Test
    void findUserById_WhenUserExists_ShouldReturnUser() {
        Account account = new Account(1);
        when(accountRepository.findById(1)).thenReturn(Optional.of(account));

        Account result = accountService.findById(1);

        assertEquals(1, result.getId());
    }

    @Test
    void findUserById_WhenUserDoesNotExist_ShouldThrowNotFound() {
        when(accountRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> accountService.findById(1));
    }

    @Test
    void addUser_WhenUserDoesNotExist_ShouldSave() {
        when(accountRepository.existsById(1)).thenReturn(false);

        accountService.add(1);

        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void addUser_WhenUserExists_ShouldThrowExistsException() {
        when(accountRepository.existsById(1)).thenReturn(true);

        assertThrows(ExistsException.class, () -> accountService.add(1));

        verify(accountRepository, never()).save(any());
    }

    @Test
    void deleteUserById_WhenUserExists_ShouldDelete() {
        when(accountRepository.existsById(1)).thenReturn(true);
        when(orderRepository.findByAccount_IdAndRentEndIsNull(1)).thenReturn(new ArrayList<Order>());

        accountService.deleteById(1);

        verify(accountRepository).deleteById(1);
    }

    @Test
    void deleteUserById_WhenUserDoesNotExist_ShouldThrowNotFound() {
        when(accountRepository.existsById(1)).thenReturn(false);
        when(orderRepository.findByAccount_IdAndRentEndIsNull(1)).thenReturn(new ArrayList<Order>());

        assertThrows(NotFoundException.class, () -> accountService.deleteById(1));

        verify(accountRepository, never()).deleteById(any());
    }

    @Test
    void changeBalanceOn_WhenEnoughFunds_ShouldUpdateBalance() {
        Account account = new Account(1);
        account.setBalanceUSD(new BigDecimal("10.00"));

        when(accountRepository.findById(1)).thenReturn(Optional.of(account));

        Account result = accountService.changeBalanceOn(1, new BigDecimal("-5.00"));

        assertEquals(new BigDecimal("5.00"), result.getBalanceUSD());
    }

    @Test
    void changeBalanceOn_WhenNotEnoughFunds_ShouldThrowRentException() {
        Account account = new Account(1);
        account.setBalanceUSD(new BigDecimal("3.00"));

        when(accountRepository.findById(1)).thenReturn(Optional.of(account));

        assertThrows(RentException.class,
                () -> accountService.changeBalanceOn(1, new BigDecimal("-5.00")));
    }
}

