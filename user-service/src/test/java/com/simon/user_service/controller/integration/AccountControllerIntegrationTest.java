package com.simon.user_service.controller.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simon.name.Headers;
import com.simon.name.PathRoles;
import com.simon.user_service.model.Account;
import com.simon.user_service.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired AccountRepository accountRepository;
    @Autowired ObjectMapper objectMapper;

    static final String URI = "/api/v1/account";

    @BeforeEach
    void setup() {
        accountRepository.deleteAll();
    }

    @Test
    void addUser_ShouldReturnOk() throws Exception {
        mockMvc.perform(post(URI + PathRoles.SERVICE + "/add/1"))
                .andExpect(status().isOk());
    }

    @Test
    void addUser_WhenExists_ShouldReturnConflict() throws Exception {
        accountRepository.save(new Account(1));

        mockMvc.perform(post(URI + PathRoles.SERVICE + "/add/1"))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteUser_WhenExists_ShouldReturnOk() throws Exception {
        accountRepository.save(new Account(1));

        mockMvc.perform(delete(URI + PathRoles.SERVICE + "/delete/1"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteUser_WhenNotExists_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(delete(URI + PathRoles.SERVICE + "/delete/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBalance_WhenValidHeader_ShouldReturnOk() throws Exception {
        accountRepository.save(new Account(1));

        mockMvc.perform(get(URI + PathRoles.USER + "/balance")
                        .header(Headers.USER_ID, "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balanceUSD").exists());
    }

    @Test
    void getBalance_WhenNoHeader_ShouldReturnConflict() throws Exception {
        mockMvc.perform(get(URI + PathRoles.USER + "/balance"))
                .andExpect(status().isConflict());
    }

    @Test
    void changeBalance_WhenValid_ShouldReturnUpdatedBalance() throws Exception {
        Account acc = new Account(1);
        acc.setBalanceUSD(new BigDecimal("10.00"));
        accountRepository.save(acc);

        mockMvc.perform(put(URI + PathRoles.USER + "/balance")
                        .param("change_on", "5.25")
                        .header(Headers.USER_ID, "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balanceUSD").value("15.25"));
    }

    @Test
    void changeBalance_WhenInsufficientFunds_ShouldReturnBadRequest() throws Exception {
        Account acc = new Account(1);
        acc.setBalanceUSD(new BigDecimal("3.00"));
        accountRepository.save(acc);

        mockMvc.perform(put(URI + PathRoles.USER + "/balance")
                        .param("change_on", "-10.00")
                        .header(Headers.USER_ID, "1"))
                .andExpect(status().isBadRequest());
    }
}
