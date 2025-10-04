package com.simon.user_service.serivce.integration;

import com.simon.exception.ExistsException;
import com.simon.exception.NotFoundException;
import com.simon.user_service.exception.RentException;
import com.simon.user_service.model.Account;
import com.simon.user_service.repository.AccountRepository;
import com.simon.user_service.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
public class AccountServiceIntegrationTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    void setup() {
        accountRepository.deleteAll();
    }

    @Test
    void findUserById_WhenUserExists_ReturnsAccount() {
        accountRepository.save(new Account(1));

        Account found = accountService.findById(1);

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(1);
    }

    @Test
    void findUserById_WhenNotExists_ThrowsNotFoundException() {
        assertThatThrownBy(() -> accountService.findById(999))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Doesn't exists account with id: " + 999);
    }

    @Test
    void addUser_WhenValid_SavesSuccessfully() {
        accountService.add(1);

        assertThat(accountRepository.existsById(1)).isTrue();
    }

    @Test
    void addUser_WhenDuplicate_ThrowsExistsException() {
        accountService.add(1);

        assertThatThrownBy(() -> accountService.add(1))
                .isInstanceOf(ExistsException.class)
                .hasMessageContaining("Already exists account with id: " + 1);
    }

    @Test
    void deleteUserById_WhenExists_DeletesSuccessfully() {
        accountService.add(1);

        accountService.deleteById(1);

        assertThat(accountRepository.existsById(1)).isFalse();
    }

    @Test
    void deleteUserById_WhenNotExists_ThrowsException() {
        assertThatThrownBy(() -> accountService.deleteById(999))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void changeBalanceOn_WhenEnoughFunds_UpdatesBalance() {
        Account account = new Account(1);
        account.setBalanceUSD(BigDecimal.valueOf(10.00));
        accountRepository.save(account);

        Account updated = accountService.changeBalanceOn(1, BigDecimal.valueOf(-5.00));

        assertThat(updated.getBalanceUSD()).isEqualByComparingTo("5.00");
    }

    @Test
    void changeBalanceOn_WhenInsufficientFunds_ThrowsRentException() {
        Account account = new Account(1);
        account.setBalanceUSD(BigDecimal.valueOf(3.00));
        accountRepository.save(account);

        assertThatThrownBy(() -> accountService.changeBalanceOn(1, BigDecimal.valueOf(-5.00)))
                .isInstanceOf(RentException.class)
                .hasMessageContaining("insufficient funds");
    }
}
