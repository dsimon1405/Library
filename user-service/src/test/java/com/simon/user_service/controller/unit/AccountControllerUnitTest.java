package com.simon.user_service.controller.unit;

import com.simon.name.Headers;
import com.simon.user_service.controller.AccountController;
import com.simon.user_service.model.Account;
import com.simon.dto.user.AccountDTO;
import com.simon.user_service.service.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountControllerUnitTest {

    @Mock
    private AccountService accountService;

    @Mock
    private HttpServletRequest httpRequest;

    @InjectMocks
    private AccountController accountController;

    @Test
    void addUser_ShouldCallService() {
        accountController.addUser(1);
        verify(accountService).add(1);
    }

    @Test
    void deleteUserById_ShouldCallService() {
        accountController.deleteUserById(5);
        verify(accountService).deleteById(5);
    }

    @Test
    void getBalance_ShouldReturnAccountDTO() {
        when(httpRequest.getHeader(Headers.USER_ID)).thenReturn("7");
        Account account = new Account(7);
        account.setBalanceUSD(new BigDecimal("20.00"));
        when(accountService.findById(7)).thenReturn(account);

        ResponseEntity<AccountDTO> response = accountController.getBalance(httpRequest);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("20.00", response.getBody().balanceUSD().toString());
        verify(accountService).findById(7);
    }

    @Test
    void changeBalanceOn_ShouldReturnUpdatedAccount() {
        when(httpRequest.getHeader(Headers.USER_ID)).thenReturn("3");
        Account updated = new Account(3);
        updated.setBalanceUSD(new BigDecimal("15.00"));
        when(accountService.changeBalanceOn(3, new BigDecimal("5.00"))).thenReturn(updated);

        ResponseEntity<AccountDTO> response = accountController.changeBalanceOn(new BigDecimal("5.00"), httpRequest);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("15.00", response.getBody().balanceUSD().toString());
        verify(accountService).changeBalanceOn(3, new BigDecimal("5.00"));
    }
}
