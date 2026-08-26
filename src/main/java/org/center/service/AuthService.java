package org.center.service;

import org.center.model.Account;
import org.center.repository.AccountRepository;
import org.center.util.PasswordUtil;

import java.time.LocalDateTime;
import java.util.Optional;

public class AuthService {

    private final AccountRepository accountRepository;

    public AuthService() {
        this(new AccountRepository());
    }

    public AuthService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Optional<Account> login(String username, String password) {
        Optional<Account> found = accountRepository.findByUsername(username);
        if (found.isEmpty()) {
            return Optional.empty();
        }
        Account account = found.get();
        if (!account.isActive() || !PasswordUtil.verify(password, account.getPasswordHash())) {
            account.setFailedLoginCount(account.getFailedLoginCount() + 1);
            accountRepository.update(account);
            return Optional.empty();
        }
        account.setFailedLoginCount(0);
        account.setLastLoginAt(LocalDateTime.now());
        accountRepository.update(account);
        return Optional.of(account);
    }
}
