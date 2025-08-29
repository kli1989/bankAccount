package com.example.bankingsystem.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends BankAccountException {

    public InsufficientFundsException(String accountNumber, BigDecimal requestedAmount, BigDecimal availableBalance) {
        super(String.format("Insufficient funds in account %s. Requested: %s, Available: %s",
                accountNumber, requestedAmount, availableBalance));
    }
}
