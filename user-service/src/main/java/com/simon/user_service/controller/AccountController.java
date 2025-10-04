package com.simon.user_service.controller;

import com.simon.name.PathRoles;
import com.simon.dto.user.AccountDTO;
import com.simon.user_service.model.Account;
import com.simon.user_service.service.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping(PathRoles.SERVICE + "/add/{id}")
    public void addUser(@PathVariable int id) {
        accountService.add(id);
    }

    @DeleteMapping(PathRoles.SERVICE + "/delete/{id}")
    public void deleteUserById(@PathVariable int id) {
        accountService.deleteById(id);
    }

    @GetMapping(PathRoles.USER + "/balance")
    public ResponseEntity<AccountDTO> getBalance(HttpServletRequest request) {
        return ResponseEntity.ok()
                .body(Account.toDTO(accountService.findById(ControllerUtil.getUserIdFromHeader(request))));
    }

    @PutMapping(PathRoles.USER + "/balance")
    public ResponseEntity<AccountDTO> changeBalanceOn(
            @RequestParam(value = "change_on") BigDecimal change_on,
            HttpServletRequest request) {
        return ResponseEntity.ok().body(
                Account.toDTO(accountService.changeBalanceOn(ControllerUtil.getUserIdFromHeader(request), change_on)));
    }

    @GetMapping(PathRoles.ADMIN + "/all")
    public List<AccountDTO> getAccounts() {
        return accountService.findAll().stream().map(Account::toDTO).toList();
    }
}
