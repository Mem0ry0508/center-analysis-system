package org.center.ui;

import org.center.model.Account;

public final class Session {

    private static Account currentAccount;

    private Session() {
    }

    public static Account getCurrentAccount() {
        return currentAccount;
    }

    public static void setCurrentAccount(Account account) {
        currentAccount = account;
    }

    public static void clear() {
        currentAccount = null;
    }
}
