package org.center.ui;

import org.center.model.Account;
import org.center.util.AuditContext;

public final class Session {

    private static Account currentAccount;

    private Session() {
    }

    public static Account getCurrentAccount() {
        return currentAccount;
    }

    public static void setCurrentAccount(Account account) {
        currentAccount = account;
        AuditContext.setActorId(account == null ? null : account.getAccountId());
    }

    public static void clear() {
        currentAccount = null;
        AuditContext.clear();
    }
}
