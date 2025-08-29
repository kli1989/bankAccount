package com.example.bankingsystem.exception;

public class AccountNotFoundException extends BankAccountException {

    public AccountNotFoundException(String accountNumber) {
        super("Account not found with account number: " + accountNumber);
    }
}
