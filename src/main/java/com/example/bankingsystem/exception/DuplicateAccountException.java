package com.example.bankingsystem.exception;

public class DuplicateAccountException extends BankAccountException {

    public DuplicateAccountException(String accountNumber) {
        super("Account already exists with account number: " + accountNumber);
    }
}
