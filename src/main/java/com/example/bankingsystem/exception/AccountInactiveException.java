package com.example.bankingsystem.exception;

import com.example.bankingsystem.entity.BankAccount;

public class AccountInactiveException extends BankAccountException {

    public AccountInactiveException(String accountNumber, BankAccount.AccountStatus status) {
        super(String.format("Account %s is not active. Current status: %s", accountNumber, status));
    }
}
