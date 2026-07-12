package com.gamingplatform.controller;

import com.gamingplatform.security.CurrentUserService;
import com.gamingplatform.service.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/account")
public class AccountController {
    private final AccountService accounts;
    private final CurrentUserService currentUser;

    public AccountController(AccountService accounts, CurrentUserService currentUser) {
        this.accounts = accounts;
        this.currentUser = currentUser;
    }

    @GetMapping("/export")
    public Map<String, Object> export() {
        return accounts.export(currentUser.requireUserId());
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(HttpServletRequest request) {
        accounts.delete(currentUser.requireUserId());
        if (request.getSession(false) != null) request.getSession(false).invalidate();
    }
}
